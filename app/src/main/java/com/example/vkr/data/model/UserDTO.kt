package com.example.vkr.data.model

data class UserDTO(
    val name: String,
    val nickname: String,
    val phone: String,
    val password: String,
    val role: String = "USER"
)