package com.example.vkr.data.remote

import com.example.vkr.data.model.UserDTO
import com.example.vkr.data.model.UserLoginDTO
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/auth/register")
    suspend fun register(@Body user: UserDTO): Response<ResponseBody>

    @POST("/auth/login")
    suspend fun login(@Body credentials: UserLoginDTO): Response<ResponseBody>
}