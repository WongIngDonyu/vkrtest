package com.example.vkr.data.repository

import com.example.vkr.data.model.RegisterDTO
import com.example.vkr.data.model.UserDTO
import com.example.vkr.data.model.UserLoginDTO
import com.example.vkr.data.remote.AuthApi
import okhttp3.ResponseBody
import retrofit2.Response

class AuthRepository(private val api: AuthApi) {
    suspend fun register(user: RegisterDTO): Response<ResponseBody> {
        return api.register(user)
    }

    suspend fun login(credentials: UserLoginDTO): Response<ResponseBody> {
        return api.login(credentials)
    }

    suspend fun getUserByPhone(phone: String): Response<UserDTO> {
        return api.getUserByPhone(phone)
    }
}