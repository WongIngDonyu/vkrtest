package com.example.vkr.data.remote

import com.example.vkr.data.model.TeamDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface  TeamApi {
    @GET("/teams")
    suspend fun getAllTeams(): Response<List<TeamDTO>>

    @PUT("teams/{teamId}/join/{userId}")
    suspend fun joinTeam(
        @Path("teamId") teamId: String,
        @Path("userId") userId: String
    ): Response<Void>

    @PUT("teams/{teamId}/leave/{userId}")
    suspend fun leaveTeam(
        @Path("teamId") teamId: String,
        @Path("userId") userId: String
    ): Response<Void>
}