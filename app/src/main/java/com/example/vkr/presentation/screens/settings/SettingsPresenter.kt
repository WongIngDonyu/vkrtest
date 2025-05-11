package com.example.vkr.presentation.screens.settings

import com.example.vkr.data.session.UserSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsPresenter(
    private val view: SettingsContract.View,
    private val session: UserSessionManager
) : SettingsContract.Presenter {

    private var state = SettingsContract.ViewState()

    override fun onInit() {
        // Здесь можно подгрузить настройки из DataStore или базы (пока статика)
        view.updateState(state)
    }

    override fun onToggle(name: String, value: Boolean) {
        state = when (name) {
            "push" -> state.copy(pushEnabled = value)
            "email" -> state.copy(emailEnabled = value)
            "reminders" -> state.copy(remindersEnabled = value)
            "location" -> state.copy(locationEnabled = value)
            "camera" -> state.copy(cameraAccessEnabled = value)
            "dark" -> state.copy(darkModeEnabled = value)
            else -> state
        }
        view.updateState(state)
    }

    override fun onLogoutClick() {
        state = state.copy(showLogoutDialog = true)
        view.updateState(state)
    }

    override fun onLogoutCancel() {
        state = state.copy(showLogoutDialog = false)
        view.updateState(state)
    }

    override fun onLogoutConfirm() {
        CoroutineScope(Dispatchers.IO).launch {
            session.clearSession()
            withContext(Dispatchers.Main) {
                view.navigateToWelcome()
            }
        }
    }
}
