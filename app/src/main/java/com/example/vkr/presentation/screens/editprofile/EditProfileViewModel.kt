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
import com.example.vkr.data.repository.UserRepository
import com.example.vkr.data.session.UserSessionManager
import com.example.vkr.ui.components.copyImageToInternalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val repository = UserRepository(
        userDao = AppDatabase.getInstance(context).userDao(),
        teamDao = AppDatabase.getInstance(context).teamDao(),
        session = UserSessionManager(context)
    )

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
        viewModelScope.launch {
            val loaded = repository.loadUserFromApi(phone)
            if (loaded != null) {
                user = loaded
                fullName = loaded.name
                username = loaded.nickname
                this@EditProfileViewModel.phone = loaded.phone
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

        viewModelScope.launch {
            val success = repository.updateUser(
                user = user!!,
                fullName = fullName,
                nickname = username,
                newAvatarUri = avatarUri,
                context = context
            )
            if (success) {
                loadUserByPhone(user!!.phone)
                withContext(Dispatchers.Main) { onSuccess() }
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