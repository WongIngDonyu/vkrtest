package com.example.vkr.presentation.screens.splash

import com.example.vkr.data.session.UserSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SplashPresenter(
    private val view: SplashContract.View,
    private val sessionManager: UserSessionManager
) : SplashContract.Presenter {

    override fun checkSession() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(500) // Пауза
            val phone = sessionManager.userPhone.firstOrNull()
            if (phone != null) {
                view.navigateToMain()
            } else {
                view.navigateToAuth()
            }
        }
    }
}