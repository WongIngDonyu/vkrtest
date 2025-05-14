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

class LoginViewModel(application: Application, private val session: UserSessionManager, private val userDao: UserDao) : AndroidViewModel(application) {

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
                val repository = AuthRepository(RetrofitInstance.api)
                val loginResponse = repository.login(UserLoginDTO(phone, password))
                if (loginResponse.isSuccessful) {
                    val userResponse = repository.getUserByPhone(phone)
                    if (userResponse.isSuccessful) {
                        val user = userResponse.body()!!
                        val entity = UserEntity(
                            id = user.id,
                            name = user.name,
                            nickname = user.nickname,
                            phone = user.phone,
                            role = user.role,
                            points = user.points,
                            eventCount = user.eventCount,
                            avatarUri = user.avatarUri,
                            teamId = user.teamId
                        )
                        userDao.insertUser(entity)
                        val achievementRefs = listOf(
                            UserAchievementCrossRef(user.id, 1),
                            UserAchievementCrossRef(user.id, 2)
                        )
                        userDao.insertUserAchievementCrossRefs(achievementRefs)
                        session.saveUser(user.phone, user.role)
                        navigateToHome = true
                    } else {
                        println("Ошибка при получении данных пользователя: ${userResponse.errorBody()?.string()}")
                        loginError = true
                    }
                } else if (loginResponse.code() == 401) {
                    loginError = true
                } else {
                    println("Ошибка авторизации: ${loginResponse.errorBody()?.string()}")
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

class LoginViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            val context = application.applicationContext
            val db = AppDatabase.getInstance(context)
            return LoginViewModel(application, UserSessionManager(context), db.userDao()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}