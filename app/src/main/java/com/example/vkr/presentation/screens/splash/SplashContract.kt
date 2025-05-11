package com.example.vkr.presentation.screens.splash

interface SplashContract {
    interface View {
        fun navigateToMain()
        fun navigateToAuth()
    }

    interface Presenter {
        fun checkSession()
    }
}