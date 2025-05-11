package com.example.vkr.presentation.screens.profile

import android.content.Context
import com.example.vkr.data.AppDatabase
import com.example.vkr.data.dao.TeamDao
import com.example.vkr.data.dao.UserDao
import com.example.vkr.data.model.AchievementEntity
import com.example.vkr.data.model.EventEntity
import com.example.vkr.data.model.UserEntity
import com.example.vkr.data.session.UserSessionManager
import com.example.vkr.ui.components.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ProfilePresenter(
    private val view: ProfileContract.View,
    private val context: Context
) : ProfileContract.Presenter {
    private val userDao = AppDatabase.getInstance(context).userDao()
    private val teamDao = AppDatabase.getInstance(context).teamDao()
    private val session = UserSessionManager(context)

    private val _state = MutableStateFlow(ProfileContract.ViewState())
    override val state: StateFlow<ProfileContract.ViewState> = _state.asStateFlow()

    override fun onInit() {
        CoroutineScope(Dispatchers.IO).launch {
            val phone = session.userPhone.firstOrNull() ?: return@launch
            val user = userDao.getUserByPhone(phone) ?: return@launch
            val achievements = userDao.getUserWithAchievements(user.id).first().achievements
            val events = userDao.getUserWithEvents(user.id).first().events
            val team = user.teamId?.let { teamDao.getAllTeams().firstOrNull { t -> t.id == it } }
            _state.value = _state.value.copy(user = user, achievements = achievements, events = events, team = team)
        }
    }

    override fun onEventSelected(event: EventEntity) {
        _state.value = _state.value.copy(selectedEvent = event)
    }

    override fun onDateFilterSelected(filter: String) {
        _state.value = _state.value.copy(selectedDateFilter = filter)
    }

    override fun onDialogDismissed() {
        _state.value = _state.value.copy(selectedEvent = null)
    }

    override fun onEditProfileClicked() {
        view.navigateToEditProfile()
    }

    override fun onSettingsClicked() {
        view.navigateToSettings()
    }
}
