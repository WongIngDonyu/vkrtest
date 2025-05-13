package com.example.vkr.data.repository

import com.example.vkr.data.dao.EventDao
import com.example.vkr.data.model.EventEntity
import com.example.vkr.data.remote.EventApi

class EventRepository(
    private val api: EventApi,
    private val eventDao: EventDao
) {
    suspend fun fetchAndSaveEvents() {
        val response = api.getAllEvents()
        if (response.isSuccessful) {
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
                    teamId = null, // teamId отсутствует в DTO, только teamName
                    imageUri = dto.imageUri.firstOrNull(),
                    isFinished = dto.finished
                )
            }

            eventDao.insertEvents(entities)
        } else {
            println("❗ Ошибка при загрузке событий: ${response.errorBody()?.string()}")
        }
    }
}