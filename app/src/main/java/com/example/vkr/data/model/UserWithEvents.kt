package com.example.vkr.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class UserWithEvents(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            UserEventCrossRef::class,
            parentColumn = "userId",
            entityColumn = "eventId"
        )
    )
    val events: List<EventEntity>
)