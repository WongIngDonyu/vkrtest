package com.example.vkr.presentation.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr.data.dao.UserDao
import com.example.vkr.data.model.UserAchievementCrossRef
import com.example.vkr.data.model.UserEntity
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
                val repository = AuthRepository(RetrofitInstance.api)
                val loginResponse = repository.login(UserLoginDTO(phone, password))

                if (loginResponse.isSuccessful) {
                    // получаем полные данные пользователя
                    val userResponse = repository.getUserByPhone(phone)

                    if (userResponse.isSuccessful) {
                        val user = userResponse.body()!!

                        // сохраняем в Room
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

                        // сохраняем только ключевые данные в сессию
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