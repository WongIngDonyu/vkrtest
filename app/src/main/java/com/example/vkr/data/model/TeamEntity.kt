package com.example.vkr.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: Int,
    val areaPoints: String,
    val points: Int = 0
)