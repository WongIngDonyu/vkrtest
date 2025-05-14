package com.example.vkr.presentation.screens.login

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vkr.data.AppDatabase
import com.example.vkr.data.dao.UserDao
import com.example.vkr.data.model.UserAchievementCrossRef
import com.example.vkr.data.model.UserEntity
import com.example.vkr.data.model.UserLoginDTO
import com.example.vkr.data.remote.RetrofitInstance
import com.example.vkr.data.repository.AuthRepository
import com.example.vkr.data.session.UserSessionManager
import kotlinx.coroutines.launch

class LoginViewModel(application: Application, private val repository: AuthRepository) : AndroidViewModel(application) {

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
            val success = repository.loginAndStoreUser(phone, password)
            loginError = !success
            if (success) {
                navigateToHome = true
            }
        }
    }

    fun onNavigationHandled() {
        navigateToHome = false
    }
}

class LoginViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            val context = application.applicationContext
            val db = AppDatabase.getInstance(context)
            val repository = AuthRepository(
                api = RetrofitInstance.api,
                userDao = db.userDao(),
                session = UserSessionManager(context)
            )
            return LoginViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}