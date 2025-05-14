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
import com.example.vkr.data.session.UserSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TeamDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val teamDao = AppDatabase.getInstance(context).teamDao()
    private val userDao = AppDatabase.getInstance(context).userDao()
    private val eventDao = AppDatabase.getInstance(context).eventDao()
    private val session = UserSessionManager(context)

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

    fun loadTeam(teamId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val t = teamDao.getAllTeams().firstOrNull { it.id == teamId }
            val u = teamDao.getUsersByTeam(teamId)
            val phone = session.userPhone.firstOrNull()
            val current = phone?.let { userDao.getUserByPhone(it) }

            val response = try {
                RetrofitInstance.eventApi.getEventsByTeam(teamId)
            } catch (e: Exception) {
                null
            }
            val eventList = if (response?.isSuccessful == true) {
                val dtos = response.body() ?: emptyList()
                val entities = dtos.map { dto ->
                    EventEntity(
                        id = dto.id,
                        title = dto.title,
                        description = dto.description,
                        locationName = dto.locationName,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        dateTime = dto.dateTime,
                        creatorId = dto.creatorId,
                        teamId = dto.teamId,
                        imageUri = dto.imageUri.firstOrNull(),
                        isFinished = dto.finished,
                        isFavorite = dto.favorite
                    )
                }
                eventDao.insertEvents(entities)
                entities
            } else {
                emptyList()
            }
            withContext(Dispatchers.Main) {
                team = t
                users = u
                events = eventList
                currentUser = current
            }
        }
    }

    fun joinTeam() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = currentUser ?: return@launch
            val tid = team?.id ?: return@launch
            try {
                val response = RetrofitInstance.teamApi.joinTeam(tid, user.id)
                if (response.isSuccessful) {
                    val updatedUserResponse = RetrofitInstance.api.getUserByPhone(user.phone)
                    if (updatedUserResponse.isSuccessful) {
                        val updatedUser = updatedUserResponse.body()
                        if (updatedUser != null) {
                            userDao.insertUser(
                                UserEntity(
                                    id = updatedUser.id,
                                    name = updatedUser.name,
                                    nickname = updatedUser.nickname,
                                    phone = updatedUser.phone,
                                    role = updatedUser.role,
                                    points = updatedUser.points,
                                    eventCount = updatedUser.eventCount,
                                    avatarUri = updatedUser.avatarUri,
                                    teamId = updatedUser.teamId
                                )
                            )
                        }
                    }
                    loadTeam(tid)
                } else {
                    Log.e("TeamJoin", "Failed to join team: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("TeamJoin", "Error joining team", e)
            }
        }
    }

    fun leaveTeam() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = currentUser ?: return@launch
            val tid = team?.id ?: return@launch
            try {
                val response = RetrofitInstance.teamApi.leaveTeam(tid, user.id)
                if (response.isSuccessful) {
                    val updatedUserResponse = RetrofitInstance.api.getUserByPhone(user.phone)
                    if (updatedUserResponse.isSuccessful) {
                        val updatedUser = updatedUserResponse.body()
                        if (updatedUser != null) {
                            userDao.insertUser(
                                UserEntity(
                                    id = updatedUser.id,
                                    name = updatedUser.name,
                                    nickname = updatedUser.nickname,
                                    phone = updatedUser.phone,
                                    role = updatedUser.role,
                                    points = updatedUser.points,
                                    eventCount = updatedUser.eventCount,
                                    avatarUri = updatedUser.avatarUri,
                                    teamId = updatedUser.teamId
                                )
                            )
                        }
                    }
                    loadTeam(tid)
                } else {
                    Log.e("TeamLeave", "Failed to leave team: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("TeamLeave", "Error leaving team", e)
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
