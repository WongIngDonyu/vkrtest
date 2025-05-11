package com.example.vkr.presentation.screens.editprofile

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.vkr.data.dao.UserDao
import com.example.vkr.data.model.UserEntity
import com.example.vkr.data.session.UserSessionManager
import com.example.vkr.ui.components.copyImageToInternalStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfilePresenter(
    private val context: Context,
    private val userDao: UserDao,
    private val sessionManager: UserSessionManager
) : EditProfileContract.Presenter {

    override var uiState by mutableStateOf(EditProfileContract.UiState())
        private set

    override suspend fun loadUser(phone: String) {
        val user = userDao.getUserByPhone(phone)
        uiState = uiState.copy(
            user = user,
            fullName = user?.name.orEmpty(),
            username = user?.nickname.orEmpty(),
            phone = user?.phone.orEmpty()
        )
    }

    override fun onFullNameChange(value: String) {
        uiState = uiState.copy(fullName = value, fullNameError = false)
    }

    override fun onUsernameChange(value: String) {
        uiState = uiState.copy(username = value, usernameError = false)
    }

    override fun onPhoneChange(value: String) {
        uiState = uiState.copy(phone = value, phoneError = false)
    }

    override fun onAvatarChange(uri: Uri?) {
        uiState = uiState.copy(avatarUri = uri)
    }

    override suspend fun save(onSuccess: () -> Unit) {
        val errors = validate()
        if (errors == null && uiState.user != null) {
            val avatarPath = uiState.avatarUri?.let {
                copyImageToInternalStorage(context, it)
            } ?: uiState.user!!.avatarUri

            val updatedUser = uiState.user!!.copy(
                name = uiState.fullName,
                nickname = uiState.username,
                phone = uiState.phone,
                avatarUri = avatarPath
            )

            userDao.updateUser(updatedUser)
            sessionManager.saveUser(uiState.phone, updatedUser.role)
            onSuccess()
        } else {
            uiState = uiState.copy(
                fullNameError = errors?.first == "fullName",
                usernameError = errors?.first == "username",
                phoneError = errors?.first == "phone"
            )
        }
    }

    private fun validate(): Pair<String, String>? {
        return when {
            uiState.fullName.isBlank() -> "fullName" to "Имя не может быть пустым"
            uiState.username.length < 3 -> "username" to "Минимум 3 символа"
            uiState.phone.isBlank() || !uiState.phone.matches(Regex("""\+?\d+""")) ->
                "phone" to "Некорректный номер"
            else -> null
        }
    }
}