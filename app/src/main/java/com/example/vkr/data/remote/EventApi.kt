package com.example.vkr.data.remote

import com.example.vkr.data.model.EventRequestDTO
import com.example.vkr.data.model.EventResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface EventApi {
    @POST("/events")
    suspend fun createEvent(@Body event: EventRequestDTO): Response<EventResponseDTO>

    @GET("/events")
    suspend fun getAllEvents(): Response<List<EventResponseDTO>>

    @POST("/events/{eventId}/join/{userId}")
    suspend fun joinEvent(@Path("eventId") eventId: String, @Path("userId") userId: String): Response<Void>

    @DELETE("/events/{eventId}/leave/{userId}")
    suspend fun leaveEvent(@Path("eventId") eventId: String, @Path("userId") userId: String): Response<Void>

    @GET("/events/team/{teamId}")
    suspend fun getEventsByTeam(@Path("teamId") teamId: String): Response<List<EventResponseDTO>>

    @PUT("/events/{id}/finish")
    suspend fun finishEvent(@Path("id") eventId: String): Response<Void>
}