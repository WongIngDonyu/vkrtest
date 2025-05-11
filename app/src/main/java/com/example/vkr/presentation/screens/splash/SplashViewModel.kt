package com.example.vkr.presentation.screens.splash

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr.data.session.UserSessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val session = UserSessionManager(application.applicationContext)

    var shouldNavigate by mutableStateOf<NavigationTarget?>(null)
        private set

    sealed class NavigationTarget {
        object Main : NavigationTarget()
        object Auth : NavigationTarget()
    }

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            delay(500)
            val phone = session.userPhone.firstOrNull()
            shouldNavigate = if (phone != null) {
                NavigationTarget.Main
            } else {
                NavigationTarget.Auth
            }
        }
    }
}