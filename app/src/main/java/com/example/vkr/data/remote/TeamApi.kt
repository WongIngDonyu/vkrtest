package com.example.vkr.data.remote

import com.example.vkr.data.model.TeamDTO
import retrofit2.Response
import retrofit2.http.GET

interface  TeamApi {
    @GET("/teams")
    suspend fun getAllTeams(): Response<List<TeamDTO>>
}