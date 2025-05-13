package com.example.vkr.data.model

data class EventRequestDTO(
    val title: String,
    val description: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val dateTime: String,
    val creatorId: String,
    val teamId: String?, // ⬅ отправляем ID
    val imageUri: List<String> = emptyList(),
    val isFinished: Boolean = false
)