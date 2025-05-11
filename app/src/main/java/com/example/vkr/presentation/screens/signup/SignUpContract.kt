package com.example.vkr.presentation.signup

interface SignUpContract {
    interface View {
        fun showValidationErrors(errors: ValidationErrors)
        fun navigateToLogin()
    }

    interface Presenter {
        fun onSignUpClicked(
            name: String,
            nickname: String,
            phone: String,
            password: String,
            confirmPassword: String,
            role: String
        )
    }

    data class ValidationErrors(
        val nameError: Boolean = false,
        val nicknameError: Boolean = false,
        val phoneError: Boolean = false,
        val passwordError: Boolean = false,
        val confirmPasswordError: Boolean = false,
        val roleError: Boolean = false
    )
}