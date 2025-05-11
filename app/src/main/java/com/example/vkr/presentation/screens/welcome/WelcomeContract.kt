package com.example.vkr.presentation.screens.welcome

interface WelcomeContract {
    interface View {
        fun navigateToSignUp()
        fun navigateToLogin()
    }

    interface Presenter {
        fun onSignUpClicked()
        fun onLoginClicked()
    }
}