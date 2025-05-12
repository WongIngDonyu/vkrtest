package com.example.vkr.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = TeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.SET_NULL // если команду удалить, команда у события обнулится
        )
    ]
)
data class EventEntity(
    @PrimaryKey val id: String, // <-- UUID
    val title: String,
    val description: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val dateTime: String,
    val creatorId: String,
    val isFavorite: Boolean = false,
    val teamId: String? = null,
    val imageUri: String? = null,
    val isFinished: Boolean = false // ✅ новое поле
)