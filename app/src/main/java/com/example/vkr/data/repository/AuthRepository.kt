package com.example.vkr.data.repository

import com.example.vkr.data.dao.UserDao
import com.example.vkr.data.model.RegisterDTO
import com.example.vkr.data.model.UserAchievementCrossRef
import com.example.vkr.data.model.UserDTO
import com.example.vkr.data.model.UserEntity
import com.example.vkr.data.model.UserLoginDTO
import com.example.vkr.data.remote.AuthApi
import com.example.vkr.data.session.UserSessionManager
import okhttp3.ResponseBody
import retrofit2.Response

class AuthRepository(private val api: AuthApi, private val userDao: UserDao, private val session: UserSessionManager) {

    suspend fun loginAndStoreUser(phone: String, password: String): Boolean {
        val loginResponse = api.login(UserLoginDTO(phone, password))
        if (!loginResponse.isSuccessful) {
            return false
        }

        val userResponse = api.getUserByPhone(phone)
        if (!userResponse.isSuccessful) {
            return false
        }

        val user = userResponse.body() ?: return false

        val entity = UserEntity(
            id = user.id,
            name = user.name,
            nickname = user.nickname,
            phone = user.phone,
            role = user.role,
            points = user.points,
            eventCount = user.eventCount,
            avatarUri = user.avatarUri,
            teamId = user.teamId
        )

        userDao.insertUser(entity)

        val achievementRefs = listOf(
            UserAchievementCrossRef(user.id, 1),
            UserAchievementCrossRef(user.id, 2)
        )

        userDao.insertUserAchievementCrossRefs(achievementRefs)

        session.saveUser(user.phone, user.role)
        return true
    }

    suspend fun register(user: RegisterDTO): Response<ResponseBody> {
        return api.register(user)
    }
}