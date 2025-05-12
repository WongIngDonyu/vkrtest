package com.example.vkr.presentation.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr.data.dao.UserDao
import com.example.vkr.data.model.UserAchievementCrossRef
import com.example.vkr.data.model.UserLoginDTO
import com.example.vkr.data.remote.RetrofitInstance
import com.example.vkr.data.repository.AuthRepository
import com.example.vkr.data.session.UserSessionManager
import kotlinx.coroutines.launch

class LoginViewModel(
    private val session: UserSessionManager,
    private val userDao: UserDao
) : ViewModel() {

    var phone by mutableStateOf("")
    var password by mutableStateOf("")
    var isPasswordVisible by mutableStateOf(false)

    var phoneError by mutableStateOf(false)
    var passwordError by mutableStateOf(false)
    var loginError by mutableStateOf(false)
    var navigateToHome by mutableStateOf(false)
        private set

    fun onPhoneChange(value: String) {
        phone = value
        phoneError = false
        loginError = false
    }

    fun onPasswordChange(value: String) {
        password = value
        passwordError = false
        loginError = false
    }

    fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
    }

    fun onLoginClick() {
        val isPhoneValid = phone.isNotBlank() && phone.matches(Regex("""\+?\d+"""))
        val isPasswordValid = password.isNotBlank()

        phoneError = !isPhoneValid
        passwordError = !isPasswordValid

        if (!isPhoneValid || !isPasswordValid) return

        viewModelScope.launch {
            try {
                val response = AuthRepository(RetrofitInstance.api).login(UserLoginDTO(phone, password))

                if (response.isSuccessful) {
                    session.saveUser(phone, "USER") // Пока роль "USER", можно доработать позже
                    navigateToHome = true
                } else if (response.code() == 401) {
                    loginError = true
                } else {
                    println("Ошибка авторизации: ${response.errorBody()?.string()}")
                    loginError = true
                }

            } catch (e: Exception) {
                println("Ошибка подключения: ${e.localizedMessage}")
                loginError = true
            }
        }
    }

    fun onNavigationHandled() {
        navigateToHome = false
    }
}