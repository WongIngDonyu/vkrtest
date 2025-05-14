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
    @PrimaryKey val id: String,
    val name: String,
    val nickname: String,
    val phone: String,
    val role: String,
    val points: Int = 0,
    val eventCount: Int = 0,
    val teamId: String? = null,
    val avatarUri: String? = null
)
