package com.example.vkr.presentation.screens.editprofile


import android.net.Uri
import com.example.vkr.data.model.UserEntity

interface EditProfileContract {
    data class UiState(
        val user: UserEntity? = null,
        val fullName: String = "",
        val username: String = "",
        val phone: String = "",
        val avatarUri: Uri? = null,
        val fullNameError: Boolean = false,
        val usernameError: Boolean = false,
        val phoneError: Boolean = false
    )

    interface Presenter {
        val uiState: UiState

        suspend fun loadUser(phone: String)
        fun onFullNameChange(value: String)
        fun onUsernameChange(value: String)
        fun onPhoneChange(value: String)
        fun onAvatarChange(uri: Uri?)
        suspend fun save(onSuccess: () -> Unit)
    }
}
