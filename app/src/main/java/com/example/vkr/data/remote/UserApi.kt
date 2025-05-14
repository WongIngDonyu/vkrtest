package com.example.vkr.data.remote

import com.example.vkr.data.model.UserDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApi {
    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body user: UserDTO): Response<UserDTO>
}