package com.example.vkr.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val color: Int, // Цвет команды
    val areaPoints: String, // Сериализованные координаты полигона
    val points: Int = 0 // Новое поле: количество очков
)