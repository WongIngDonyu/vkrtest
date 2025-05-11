package com.example.vkr.presentation.screens.editprofile

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr.data.AppDatabase
import com.example.vkr.data.model.UserEntity
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
    var phoneError by mutableStateOf(false)

    fun onFullNameChange(value: String) {
        fullName = value
        fullNameError = false
    }

    fun onUsernameChange(value: String) {
        username = value
        usernameError = false
    }

    fun onPhoneChange(value: String) {
        phone = value
        phoneError = false
    }

    fun onAvatarChange(uri: Uri?) {
        avatarUri = uri
    }

    fun loadUserByPhone(phone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val loadedUser = userDao.getUserByPhone(phone)
            withContext(Dispatchers.Main) {
                user = loadedUser
                fullName = loadedUser?.name.orEmpty()
                username = loadedUser?.nickname.orEmpty()
                this@EditProfileViewModel.phone = loadedUser?.phone.orEmpty()
            }
        }
    }

    fun save(onSuccess: () -> Unit) {
        val validationResult = validate()
        if (validationResult != null || user == null) {
            fullNameError = validationResult == "fullName"
            usernameError = validationResult == "username"
            phoneError = validationResult == "phone"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val avatarPath = avatarUri?.let {
                copyImageToInternalStorage(context, it)
            } ?: user!!.avatarUri

            val updatedUser = user!!.copy(
                name = fullName,
                nickname = username,
                phone = phone,
                avatarUri = avatarPath
            )

            userDao.updateUser(updatedUser)
            session.saveUser(phone, updatedUser.role)

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    private fun validate(): String? {
        return when {
            fullName.isBlank() -> "fullName"
            username.length < 3 -> "username"
            phone.isBlank() || !phone.matches(Regex("""\+?\d+""")) -> "phone"
            else -> null
        }
    }
}