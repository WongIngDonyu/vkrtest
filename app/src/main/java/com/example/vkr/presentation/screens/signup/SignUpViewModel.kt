package com.example.vkr.presentation.screens.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr.data.dao.UserDao
import com.example.vkr.data.model.UserEntity
import kotlinx.coroutines.launch

class SignUpViewModel(private val userDao: UserDao) : ViewModel() {

    var name by mutableStateOf("")
    var nickname by mutableStateOf("")
    var phone by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var selectedRole by mutableStateOf("")
    var isPasswordVisible by mutableStateOf(false)

    var nameError by mutableStateOf(false)
    var nicknameError by mutableStateOf(false)
    var phoneError by mutableStateOf(false)
    var passwordError by mutableStateOf(false)
    var confirmPasswordError by mutableStateOf(false)
    var navigateToLogin by mutableStateOf(false)
        private set

    fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
    }

    fun signUp() {
        nameError = name.isBlank()
        nicknameError = nickname.length < 3
        phoneError = phone.isBlank() || !phone.matches(Regex("""\+?\d+"""))
        passwordError = password.isBlank()
        confirmPasswordError = password != confirmPassword

        if (listOf(
                nameError, nicknameError, phoneError,
                passwordError, confirmPasswordError
            ).any { it }) return

        val user = UserEntity(
            name = name,
            nickname = nickname,
            phone = phone,
            password = password,
            role = "user"
        )

        viewModelScope.launch {
            userDao.insertUser(user)
            navigateToLogin = true
        }
    }

    fun onNavigationHandled() {
        navigateToLogin = false
    }
}
