package com.example.vkr.presentation.screens.settings

interface SettingsContract {
    data class ViewState(
        val pushEnabled: Boolean = false,
        val emailEnabled: Boolean = false,
        val remindersEnabled: Boolean = false,
        val locationEnabled: Boolean = false,
        val cameraAccessEnabled: Boolean = false,
        val darkModeEnabled: Boolean = false,
        val showLogoutDialog: Boolean = false
    )

    interface View {
        fun updateState(state: ViewState)
        fun navigateToWelcome()
    }

    interface Presenter {
        fun onInit()
        fun onToggle(name: String, value: Boolean)
        fun onLogoutClick()
        fun onLogoutConfirm()
        fun onLogoutCancel()
    }
}
