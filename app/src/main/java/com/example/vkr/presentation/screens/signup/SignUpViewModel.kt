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

    var nameErrorText by mutableStateOf("")
    var nicknameErrorText by mutableStateOf("")
    var phoneErrorText by mutableStateOf("")
    var passwordErrorText by mutableStateOf("")
    var confirmPasswordErrorText by mutableStateOf("")
    var navigateToLogin by mutableStateOf(false)
        private set

    fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
    }

    fun signUp() {
        nameErrorText = ""
        nicknameErrorText = ""
        phoneErrorText = ""
        passwordErrorText = ""
        confirmPasswordErrorText = ""
        if (name.length !in 2..50) {
            nameErrorText = "Имя должно содержать от 2 до 50 символов"
        }
        if (nickname.length !in 3..30) {
            nicknameErrorText = "Никнейм должен содержать от 3 до 30 символов"
        }
        if (!phone.matches(Regex("""\d{10,15}"""))) {
            phoneErrorText = "Телефон должен содержать от 10 до 15 цифр"
        }
        if (!password.matches(Regex("""^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$"""))) {
            passwordErrorText = "Пароль должен быть не менее 8 символов и содержать строчную, заглавную букву и цифру"
        }
        if (password != confirmPassword) {
            confirmPasswordErrorText = "Пароли не совпадают"
        }
        if (listOf(nameErrorText, nicknameErrorText, phoneErrorText, passwordErrorText, confirmPasswordErrorText).any { it.isNotEmpty() }) {
            return
        }
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