package com.example.vkr.data.repository

import com.example.vkr.data.dao.TeamDao
import com.example.vkr.data.model.TeamEntity
import com.example.vkr.data.remote.TeamApi

class TeamRepository(private val api: TeamApi, private val dao: TeamDao) {

    suspend fun fetchAndSaveTeams() {
        val response = api.getAllTeams()
        if (response.isSuccessful) {
            val teamDTOs = response.body() ?: return
            val entities = teamDTOs.map {
                TeamEntity(
                    id = it.id,
                    name = it.name,
                    color = it.color,
                    areaPoints = it.areaPoints,
                    points = it.points
                )
            }
            dao.clearTeams()
            dao.insertTeams(entities)
        } else {
            println("Ошибка загрузки команд: ${response.errorBody()?.string()}")
        }
    }
}