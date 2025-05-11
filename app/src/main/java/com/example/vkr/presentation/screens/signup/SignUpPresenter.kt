package com.example.vkr.presentation.signup

import com.example.vkr.data.dao.UserDao
import com.example.vkr.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpPresenter(
    private val view: SignUpContract.View,
    private val userDao: UserDao
) : SignUpContract.Presenter {

    override fun onSignUpClicked(
        name: String,
        nickname: String,
        phone: String,
        password: String,
        confirmPassword: String,
        role: String
    ) {
        val errors = SignUpContract.ValidationErrors(
            nameError = name.isBlank(),
            nicknameError = nickname.length < 3,
            phoneError = phone.isBlank() || !phone.matches(Regex("""\+?\d+""")),
            passwordError = password.isBlank(),
            confirmPasswordError = password != confirmPassword,
            roleError = role.isBlank()
        )

        if (
            errors.nameError || errors.nicknameError || errors.phoneError ||
            errors.passwordError || errors.confirmPasswordError || errors.roleError
        ) {
            view.showValidationErrors(errors)
            return
        }

        val user = UserEntity(
            name = name,
            nickname = nickname,
            phone = phone,
            password = password,
            role = role
        )
        CoroutineScope(Dispatchers.IO).launch {
            userDao.insertUser(user)
            withContext(Dispatchers.Main) {
                view.navigateToLogin()
            }
        }
    }
}