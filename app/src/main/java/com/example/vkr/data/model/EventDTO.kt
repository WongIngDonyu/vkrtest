package com.example.vkr.data.model

data class EventDTO(
    val id: String,
    val title: String,
    val description: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val dateTime: String,
    val creatorId: String,
    val teamId: String?,
    val imageUri: List<String>, // ← исправить вот это
    val isFinished: Boolean = false
)