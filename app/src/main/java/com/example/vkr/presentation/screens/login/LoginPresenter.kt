package com.example.vkr.presentation.screens.login

import com.example.vkr.data.dao.UserDao
import com.example.vkr.data.model.UserAchievementCrossRef
import com.example.vkr.data.session.UserSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginPresenter(
    private val view: LoginContract.View,
    private val session: UserSessionManager,
    private val userDao: UserDao
) : LoginContract.Presenter {

    override fun onLoginClicked(phone: String, password: String) {
        val phoneValid = phone.isNotBlank() && phone.matches(Regex("""\+?\d+"""))
        val passwordValid = password.isNotBlank()

        if (!phoneValid || !passwordValid) {
            view.showValidationErrors(phoneError = !phoneValid, passwordError = !passwordValid)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val user = userDao.getUserByPhone(phone)

            withContext(Dispatchers.Main) {
                if (user != null && user.password == password) {
                    session.saveUser(user.phone, user.role)
                    userDao.insertUserAchievementCrossRef(UserAchievementCrossRef(user.id, 1))
                    userDao.insertUserAchievementCrossRef(UserAchievementCrossRef(user.id, 2))
                    view.navigateToHome()
                } else {
                    view.showLoginError()
                }
            }
        }
    }
}
