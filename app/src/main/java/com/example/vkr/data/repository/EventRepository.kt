package com.example.vkr.data.repository

import com.example.vkr.data.dao.EventDao
import com.example.vkr.data.dao.TeamDao
import com.example.vkr.data.dao.UserDao
import com.example.vkr.data.model.EventEntity
import com.example.vkr.data.model.EventRequestDTO
import com.example.vkr.data.model.TeamEntity
import com.example.vkr.data.model.UserEntity
import com.example.vkr.data.model.UserEventCrossRef
import com.example.vkr.data.remote.EventApi
import kotlinx.coroutines.flow.Flow

class EventRepository(private val api: EventApi,private val eventDao: EventDao, private val teamDao: TeamDao, private val userDao: UserDao) {

    suspend fun fetchAndSaveEvents() {
        val response = api.getAllEvents()
        if (response.isSuccessful) {
            val dtos = response.body().orEmpty()
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
        } else {
            println("Ошибка при загрузке событий: ${response.errorBody()?.string()}")
        }
    }

    suspend fun getUserByPhone(phone: String): UserEntity? {
        return userDao.getUserByPhone(phone)
    }

    suspend fun getJoinedEventsForUser(userId: String): List<EventEntity> {
        return userDao.getUserWithEventsOnce(userId)?.events?.filter { !it.isFinished } ?: emptyList()
    }

    suspend fun getParticipantCounts(events: List<EventEntity>): Map<String, Int> {
        return events.associate { it.id to userDao.getUserCountForEvent(it.id) }
    }

    suspend fun toggleFavorite(event: EventEntity) {
        val updated = event.copy(isFavorite = !event.isFavorite)
        eventDao.updateEvent(updated)
    }

    suspend fun getEventWithTeamName(eventId: String): Pair<EventEntity?, String> {
        val event = eventDao.getEventById(eventId)
        val teamName = event?.teamId?.let { teamId ->
            teamDao.getAllTeams().firstOrNull { it.id == teamId }?.name ?: ""
        } ?: ""
        return event to teamName
    }

    suspend fun finishEvent(event: EventEntity): Boolean {
        return try {
            val response = api.finishEvent(event.id)
            if (response.isSuccessful) {
                val updated = event.copy(isFinished = true)
                eventDao.updateEvent(updated)
                true
            } else {
                println("Ошибка завершения события: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            println("Ошибка завершения события: ${e.localizedMessage}")
            false
        }
    }

    fun getAllEventsFlow(): Flow<List<EventEntity>> {
        return eventDao.getAllEvents()
    }

    suspend fun joinEvent(userId: String, eventId: String): Boolean {
        val response = api.joinEvent(eventId, userId)
        return if (response.isSuccessful || response.code() == 204) {
            userDao.insertUserEventCrossRef(UserEventCrossRef(userId, eventId))
            true
        } else {
            false
        }
    }

    suspend fun leaveEvent(userId: String, eventId: String): Boolean {
        val response = api.leaveEvent(eventId, userId)
        return if (response.isSuccessful || response.code() == 204) {
            userDao.deleteUserEventCrossRef(userId, eventId)
            true
        } else {
            false
        }
    }

    suspend fun getTeamById(teamId: String): TeamEntity? {
        return teamDao.getAllTeams().firstOrNull { it.id == teamId }
    }

    suspend fun createAndSaveEvent(
        creator: UserEntity,
        dto: EventRequestDTO
    ): EventEntity? {
        return try {
            val response = api.createEvent(dto)
            if (response.isSuccessful) {
                val serverEvent = response.body()
                if (serverEvent != null) {
                    val localEvent = EventEntity(
                        id = serverEvent.id,
                        title = serverEvent.title,
                        description = serverEvent.description,
                        locationName = serverEvent.locationName,
                        latitude = serverEvent.latitude,
                        longitude = serverEvent.longitude,
                        dateTime = serverEvent.dateTime,
                        creatorId = serverEvent.creatorId,
                        teamId = serverEvent.teamId,
                        imageUri = serverEvent.imageUri.firstOrNull(),
                        isFinished = serverEvent.finished
                    )
                    eventDao.insertEvent(localEvent)
                    userDao.insertUserEventCrossRef(UserEventCrossRef(creator.id, localEvent.id))
                    return localEvent
                }
            } else {
                println("Ошибка при создании события: ${response.errorBody()?.string()}")
            }
            null
        } catch (e: Exception) {
            println("Ошибка подключения: ${e.localizedMessage}")
            null
        }
    }
}
