package com.example.vkr.data.repository

import com.example.vkr.data.dao.EventDao
import com.example.vkr.data.dao.TeamDao
import com.example.vkr.data.dao.UserDao
import com.example.vkr.data.model.EventEntity
import com.example.vkr.data.model.TeamEntity
import com.example.vkr.data.model.UserDTO
import com.example.vkr.data.model.UserEntity
import com.example.vkr.data.remote.RetrofitInstance
import com.example.vkr.data.session.UserSessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class TeamRepository(private val teamDao: TeamDao, private val userDao: UserDao, private val eventDao: EventDao, private val session: UserSessionManager) {
    suspend fun loadTeamData(teamId: String): TeamData {
        val team = teamDao.getAllTeams().firstOrNull { it.id == teamId }
        val users = teamDao.getUsersByTeam(teamId)
        val phone = session.userPhone.firstOrNull()
        val currentUser = phone?.let { userDao.getUserByPhone(it) }
        val remoteEvents = try {
            RetrofitInstance.eventApi.getEventsByTeam(teamId)
        } catch (e: Exception) {
            null
        }
        val events = if (remoteEvents?.isSuccessful == true) {
            val dtos = remoteEvents.body().orEmpty()
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
        return TeamData(team, users, events, currentUser)
    }

    suspend fun joinTeam(teamId: String, user: UserEntity): UserEntity? {
        val response = RetrofitInstance.teamApi.joinTeam(teamId, user.id)
        if (response.isSuccessful) {
            val updated = RetrofitInstance.api.getUserByPhone(user.phone)
            if (updated.isSuccessful) {
                updated.body()?.let { dto ->
                    val entity = dto.toEntity()
                    userDao.insertUser(entity)
                    return entity
                }
            }
        }
        return null
    }

    suspend fun leaveTeam(teamId: String, user: UserEntity): UserEntity? {
        val response = RetrofitInstance.teamApi.leaveTeam(teamId, user.id)
        if (response.isSuccessful) {
            val updated = RetrofitInstance.api.getUserByPhone(user.phone)
            if (updated.isSuccessful) {
                updated.body()?.let { dto ->
                    val entity = dto.toEntity()
                    userDao.insertUser(entity)
                    return entity
                }
            }
        }
        return null
    }

    suspend fun syncTeamsFromRemote() {
        try {
            val response = RetrofitInstance.teamApi.getAllTeams()
            if (response.isSuccessful) {
                val teamDTOs = response.body().orEmpty()
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
    }

    fun getTeamsFlow(): Flow<List<TeamEntity>> = teamDao.getAllTeamsFlow()
}

data class TeamData(
    val team: TeamEntity?,
    val users: List<UserEntity>,
    val events: List<EventEntity>,
    val currentUser: UserEntity?
)

fun UserDTO.toEntity() = UserEntity(
    id = id,
    name = name,
    nickname = nickname,
    phone = phone,
    role = role,
    points = points,
    eventCount = eventCount,
    avatarUri = avatarUri,
    teamId = teamId
)