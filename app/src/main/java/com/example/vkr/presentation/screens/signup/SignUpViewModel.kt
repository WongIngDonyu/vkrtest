package com.example.vkr.presentation.screens.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr.data.model.RegisterDTO
import com.example.vkr.data.remote.RetrofitInstance
import com.example.vkr.data.repository.AuthRepository
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {

    private val repository = AuthRepository(RetrofitInstance.api)

    var name by mutableStateOf("")
    var nickname by mutableStateOf("")
    var phone by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
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
        if (listOf(nameError, nicknameError, phoneError, passwordError, confirmPasswordError).any { it }) return
        val user = RegisterDTO(name, nickname, phone, password)
        viewModelScope.launch {
            try {
                val response = repository.register(user)
                if (response.isSuccessful) {
                    val message = response.body()?.string() ?: "Успешно"
                    println("Ответ от сервера: $message")
                    navigateToLogin = true
                } else {
                    val errorText = response.errorBody()?.string()
                    println("Ошибка регистрации: $errorText")
                }
            } catch (e: Exception) {
                println("Ошибка подключения: ${e.localizedMessage}")
            }
        }
    }

    fun onNavigationHandled() {
        navigateToLogin = false
    }
}