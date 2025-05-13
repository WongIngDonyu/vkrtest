package com.example.vkr.data.remote

import com.example.vkr.data.model.EventRequestDTO
import com.example.vkr.data.model.EventResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface EventApi {
    @POST("/events")
    suspend fun createEvent(@Body event: EventRequestDTO): Response<EventResponseDTO>

    @GET("/events")
    suspend fun getAllEvents(): Response<List<EventResponseDTO>>
}