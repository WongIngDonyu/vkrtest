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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val teamDao = AppDatabase.getInstance(context).teamDao()
    private val teamApi = RetrofitInstance.teamApi
    private val _teams = MutableStateFlow<List<TeamEntity>>(emptyList())
    val teams: StateFlow<List<TeamEntity>> = _teams.asStateFlow()

    var showDialog by mutableStateOf(false)
        private set

    init {
        fetchAndObserveTeams()
    }

    private fun fetchAndObserveTeams() {
        viewModelScope.launch {
            // 1. Загрузить с сервера и сохранить в БД
            try {
                val response = teamApi.getAllTeams()
                if (response.isSuccessful) {
                    val teamDTOs = response.body() ?: emptyList()
                    val entities = teamDTOs.map {
                        TeamEntity(
                            id = it.id,
                            name = it.name,
                            color = it.color,
                            areaPoints = it.areaPoints,
                            points = it.points
                        )
                    }
                    teamDao.insertTeams(entities)
                } else {
                    println("Ошибка загрузки команд: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                println("Ошибка запроса команд: ${e.localizedMessage}")
            }

            // 2. Подписаться на обновления из Room
            teamDao.getAllTeamsFlow().collect { teamList ->
                _teams.value = teamList
            }
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