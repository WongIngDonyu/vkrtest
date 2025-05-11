package com.example.vkr.presentation.screens.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr.data.session.UserSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val session = UserSessionManager(application.applicationContext)

    var pushEnabled by mutableStateOf(false)
    var emailEnabled by mutableStateOf(false)
    var remindersEnabled by mutableStateOf(false)
    var locationEnabled by mutableStateOf(false)
    var cameraAccessEnabled by mutableStateOf(false)
    var darkModeEnabled by mutableStateOf(false)
    var showLogoutDialog by mutableStateOf(false)

    fun onToggle(name: String, value: Boolean) {
        when (name) {
            "push" -> pushEnabled = value
            "email" -> emailEnabled = value
            "reminders" -> remindersEnabled = value
            "location" -> locationEnabled = value
            "camera" -> cameraAccessEnabled = value
            "dark" -> darkModeEnabled = value
        }
    }

    fun onLogoutClick() {
        showLogoutDialog = true
    }

    fun onLogoutCancel() {
        showLogoutDialog = false
    }

    fun onLogoutConfirm(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            session.clearSession()
            withContext(Dispatchers.Main) {
                onLogoutSuccess()
            }
        }
    }
}