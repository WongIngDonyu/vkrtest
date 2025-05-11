package com.example.vkr.presentation.screens.welcome

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WelcomeViewModel : ViewModel() {

    private val _navEvent = MutableStateFlow<String?>(null)
    val navEvent: StateFlow<String?> = _navEvent

    fun onSignUpClicked() {
        _navEvent.value = "signup"
    }

    fun onLoginClicked() {
        _navEvent.value = "login"
    }

    fun onNavigationHandled() {
        _navEvent.value = null
    }
}