package com.example.vkr.presentation.screens.profile

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr.data.AppDatabase
import com.example.vkr.data.model.AchievementEntity
import com.example.vkr.data.model.EventEntity
import com.example.vkr.data.model.TeamEntity
import com.example.vkr.data.model.UserEntity
import com.example.vkr.data.session.UserSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val userDao = AppDatabase.getInstance(context).userDao()
    private val teamDao = AppDatabase.getInstance(context).teamDao()
    private val session = UserSessionManager(context)

    var user by mutableStateOf<UserEntity?>(null)
        private set

    var team by mutableStateOf<TeamEntity?>(null)
        private set

    var achievements by mutableStateOf<List<AchievementEntity>>(emptyList())
        private set

    var events by mutableStateOf<List<EventEntity>>(emptyList())
        private set

    var selectedDateFilter by mutableStateOf("Предстоящие")
        private set

    var selectedEvent by mutableStateOf<EventEntity?>(null)
        private set

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            val phone = session.userPhone.firstOrNull() ?: return@launch
            val loadedUser = userDao.getUserByPhone(phone) ?: return@launch
            val loadedAchievements = userDao.getUserWithAchievements(loadedUser.id).first().achievements
            val loadedEvents = userDao.getUserWithEvents(loadedUser.id).first().events
            val loadedTeam = loadedUser.teamId?.let { teamId ->
                teamDao.getAllTeams().firstOrNull { team -> team.id == teamId }
            }

            withContext(Dispatchers.Main) {
                user = loadedUser
                achievements = loadedAchievements
                events = loadedEvents
                team = loadedTeam
            }
        }
    }

    fun selectEvent(event: EventEntity) {
        selectedEvent = event
    }

    fun closeDialog() {
        selectedEvent = null
    }

    fun selectDateFilter(filter: String) {
        selectedDateFilter = filter
    }
}