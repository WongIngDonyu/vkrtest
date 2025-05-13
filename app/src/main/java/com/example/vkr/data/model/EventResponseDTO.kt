package com.example.vkr.data.model

data class EventResponseDTO(
    val id: String,
    val title: String,
    val description: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val dateTime: String,
    val creatorId: String,
    val teamId: String?,                  // ← teamId приходит отдельно
    val imageUri: List<String>,
    val verified: Boolean,
    val confirmationComment: String?,
    val teamName: String?,               // ← может прийти отдельно
    val participantCount: Int,
    val participant: List<String>?,      // ← может быть null
    val rejected: Boolean,
    val finished: Boolean,
    val favorite: Boolean
)