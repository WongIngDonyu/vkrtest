package com.example.vkr.presentation.screens.login

interface LoginContract {
    interface View {
        fun showValidationErrors(phoneError: Boolean, passwordError: Boolean)
        fun showLoginError()
        fun navigateToHome()
    }

    interface Presenter {
        fun onLoginClicked(phone: String, password: String)
    }
}