package com.example.vkr.presentation.screens.editprofile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr.data.AppDatabase
import com.example.vkr.data.model.UserDTO
import com.example.vkr.data.model.UserEntity
import com.example.vkr.data.remote.RetrofitInstance
import com.example.vkr.data.session.UserSessionManager
import com.example.vkr.ui.components.copyImageToInternalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val userDao = AppDatabase.getInstance(context).userDao()
    private val session = UserSessionManager(context)

    var user by mutableStateOf<UserEntity?>(null)
        private set

    var fullName by mutableStateOf("")
    var username by mutableStateOf("")
    var phone by mutableStateOf("")
    var avatarUri by mutableStateOf<Uri?>(null)

    var fullNameError by mutableStateOf(false)
    var usernameError by mutableStateOf(false)

    fun onFullNameChange(value: String) {
        fullName = value
        fullNameError = false
    }

    fun onUsernameChange(value: String) {
        username = value
        usernameError = false
    }

    fun onAvatarChange(uri: Uri?) {
        avatarUri = uri
    }

    fun loadUserByPhone(phone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.getUserByPhone(phone)
                if (response.isSuccessful) {
                    val userDto = response.body() ?: return@launch
                    val loadedUser = UserEntity(
                        id = userDto.id,
                        name = userDto.name,
                        nickname = userDto.nickname,
                        phone = userDto.phone,
                        role = userDto.role,
                        points = userDto.points,
                        eventCount = userDto.eventCount,
                        avatarUri = userDto.avatarUri,
                        teamId = userDto.teamId
                    )

                    withContext(Dispatchers.Main) {
                        user = loadedUser
                        fullName = loadedUser.name
                        username = loadedUser.nickname
                        this@EditProfileViewModel.phone = loadedUser.phone
                    }
                } else {
                    Log.e("EditProfile", "Ошибка загрузки пользователя: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("EditProfile", "Ошибка получения пользователя", e)
            }
        }
    }

    fun save(onSuccess: () -> Unit) {
        val validationResult = validate()
        if (validationResult != null || user == null) {
            fullNameError = validationResult == "fullName"
            usernameError = validationResult == "username"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val avatarPath = avatarUri?.let {
                copyImageToInternalStorage(context, it)
            } ?: user!!.avatarUri

            val updatedDto = UserDTO(
                id = user!!.id,
                name = fullName,
                nickname = username,
                phone = user!!.phone, // номер не редактируется
                role = user!!.role,
                points = user!!.points,
                eventCount = user!!.eventCount,
                avatarUri = avatarPath,
                teamId = user!!.teamId
            )

            try {
                val response = RetrofitInstance.userApi.updateUser(user!!.id, updatedDto)
                if (response.isSuccessful) {
                    val updatedUserDto = response.body()
                    if (updatedUserDto != null) {
                        session.saveUser(updatedUserDto.phone, updatedUserDto.role)

                        val updatedUser = UserEntity(
                            id = updatedUserDto.id,
                            name = updatedUserDto.name,
                            nickname = updatedUserDto.nickname,
                            phone = updatedUserDto.phone,
                            role = updatedUserDto.role,
                            points = updatedUserDto.points,
                            eventCount = updatedUserDto.eventCount,
                            avatarUri = updatedUserDto.avatarUri,
                            teamId = updatedUserDto.teamId
                        )

                        // 💾 Сохраняем в локальную базу
                        userDao.updateUser(updatedUser)

                        // Обновляем ViewModel
                        loadUserByPhone(updatedUser.phone)

                        withContext(Dispatchers.Main) {
                            onSuccess()
                        }
                    }
                } else {
                    Log.e("EditProfile", "Ошибка обновления: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("EditProfile", "Ошибка подключения к серверу", e)
            }
        }
    }

    private fun validate(): String? {
        return when {
            fullName.isBlank() -> "fullName"
            username.length < 3 -> "username"
            else -> null
        }
    }
}