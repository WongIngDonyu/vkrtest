package com.example.vkr.presentation.screens.search

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.vkr.data.AppDatabase
import com.example.vkr.data.model.TeamEntity
import com.example.vkr.data.remote.RetrofitInstance
import com.example.vkr.data.repository.TeamRepository
import com.example.vkr.data.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TeamRepository

    private val _teams = MutableStateFlow<List<TeamEntity>>(emptyList())
    val teams: StateFlow<List<TeamEntity>> = _teams.asStateFlow()

    var showDialog by mutableStateOf(false)
        private set

    init {
        val context = application.applicationContext
        val db = AppDatabase.getInstance(context)
        repository = TeamRepository(teamDao = db.teamDao(), userDao = db.userDao(), eventDao = db.eventDao(), session = UserSessionManager(context)
        )
        fetchAndObserveTeams()
    }

    private fun fetchAndObserveTeams() {
        viewModelScope.launch {
            repository.syncTeamsFromRemote()
            repository.getTeamsFlow().collect { _teams.value = it }
        }
    }

    fun onTeamClicked(teamId: String, navController: NavController) {
        navController.navigate("teamDetail/$teamId")
    }

    fun onShowDialog() {
        showDialog = true
    }

    fun onHideDialog() {
        showDialog = false
    }
}