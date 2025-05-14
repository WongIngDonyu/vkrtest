package com.example.vkr.presentation.screens.teamdetail

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr.data.AppDatabase
import com.example.vkr.data.model.EventEntity
import com.example.vkr.data.model.TeamEntity
import com.example.vkr.data.model.UserEntity
import com.example.vkr.data.remote.RetrofitInstance
import com.example.vkr.data.repository.TeamRepository
import com.example.vkr.data.session.UserSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TeamDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TeamRepository

    var team by mutableStateOf<TeamEntity?>(null)
        private set

    var users by mutableStateOf<List<UserEntity>>(emptyList())
        private set

    var events by mutableStateOf<List<EventEntity>>(emptyList())
        private set

    var currentUser by mutableStateOf<UserEntity?>(null)
        private set

    var selectedEvent by mutableStateOf<EventEntity?>(null)
        private set

    init {
        val context = application.applicationContext
        repository = TeamRepository(teamDao = AppDatabase.getInstance(context).teamDao(), userDao = AppDatabase.getInstance(context).userDao(), eventDao = AppDatabase.getInstance(context).eventDao(), session = UserSessionManager(context)
        )
    }

    fun loadTeam(teamId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.loadTeamData(teamId)
            withContext(Dispatchers.Main) {
                team = data.team
                users = data.users
                events = data.events
                currentUser = data.currentUser
            }
        }
    }

    fun joinTeam() {
        val user = currentUser
        val tid = team?.id
        if (user == null || tid == null) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.joinTeam(tid, user)?.let {
                loadTeam(tid)
            }
        }
    }

    fun leaveTeam() {
        val user = currentUser
        val tid = team?.id
        if (user == null || tid == null) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.leaveTeam(tid, user)?.let {
                loadTeam(tid)
            }
        }
    }

    fun selectEvent(event: EventEntity) {
        selectedEvent = event
    }

    fun onDialogClose() {
        selectedEvent = null
    }
}