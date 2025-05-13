package com.example.vkr.data.remote

import com.example.vkr.data.model.EventDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface EventApi {
    @POST("/events")
    suspend fun createEvent(@Body dto: EventDTO): Response<EventDTO>

    @GET("/events")
    suspend fun getAllEvents(): Response<List<EventDTO>>
}