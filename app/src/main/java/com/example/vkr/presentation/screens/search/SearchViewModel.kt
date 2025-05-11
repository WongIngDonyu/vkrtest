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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val teamDao = AppDatabase.getInstance(application).teamDao()

    private val _teams = MutableStateFlow<List<TeamEntity>>(emptyList())
    val teams: StateFlow<List<TeamEntity>> = _teams.asStateFlow()

    var showDialog by mutableStateOf(false)
        private set

    init {
        loadTeams()
    }

    private fun loadTeams() {
        viewModelScope.launch {
            teamDao.getAllTeamsFlow().collect { teamList ->
                _teams.value = teamList
            }
        }
    }

    fun onTeamClicked(teamId: Int, navController: NavController) {
        navController.navigate("teamDetail/$teamId")
    }

    fun onShowDialog() {
        showDialog = true
    }

    fun onHideDialog() {
        showDialog = false
    }
}