package com.example.vkr.data.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "userEventCrossRef",
    primaryKeys = ["userId", "eventId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserEventCrossRef(
    val userId: String,
    val eventId: String
)