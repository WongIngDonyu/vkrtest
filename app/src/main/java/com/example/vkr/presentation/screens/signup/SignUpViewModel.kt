package com.example.vkr.presentation.screens.signup

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vkr.data.AppDatabase
import com.example.vkr.data.model.RegisterDTO
import com.example.vkr.data.remote.RetrofitInstance
import com.example.vkr.data.repository.AuthRepository
import com.example.vkr.data.session.UserSessionManager
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val repository: AuthRepository
) : ViewModel() {

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
            passwordErrorText = "Пароль должен содержать строчную, заглавную букву и цифру и быть не менее 8 символов"
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
                    println("Регистрация успешна: ${response.body()?.string()}")
                    navigateToLogin = true
                } else {
                    println("Ошибка регистрации: ${response.errorBody()?.string()}")
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

class SignUpViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            val context = application.applicationContext
            val db = AppDatabase.getInstance(context)
            val repository = AuthRepository(
                api = RetrofitInstance.api,
                userDao = db.userDao(),
                session = UserSessionManager(context)
            )
            @Suppress("UNCHECKED_CAST")
            return SignUpViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}