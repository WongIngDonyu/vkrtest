package com.example.vkr.presentation.screens.welcome

class WelcomePresenter(private val view: WelcomeContract.View) : WelcomeContract.Presenter {
    override fun onSignUpClicked() {
        view.navigateToSignUp()
    }

    override fun onLoginClicked() {
        view.navigateToLogin()
    }
}