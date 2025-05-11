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
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val dateTime: String,
    val creatorId: Int,
    val isFavorite: Boolean = false,
    val teamId: Int? = null,
    val imageUri: String? = null,
    val isFinished: Boolean = false // ✅ новое поле
)