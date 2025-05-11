package com.example.vkr.data.model

import androidx.room.*

@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = TeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("teamId")]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val nickname: String,
    val phone: String,
    val password: String,
    val role: String,
    val points: Int = 0,
    val eventCount: Int = 0,
    val teamId: Int? = null,
    val avatarUri: String? = null // 👤 путь к изображению профиля
)
