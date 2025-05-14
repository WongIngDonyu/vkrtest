package com.example.vkr.data.repository

import android.content.Context
import android.net.Uri
import com.example.vkr.data.dao.TeamDao
import com.example.vkr.data.dao.UserDao
import com.example.vkr.data.model.AchievementEntity
import com.example.vkr.data.model.EventEntity
import com.example.vkr.data.model.TeamEntity
import com.example.vkr.data.model.UserDTO
import com.example.vkr.data.model.UserEntity
import com.example.vkr.data.remote.RetrofitInstance
import com.example.vkr.data.session.UserSessionManager
import com.example.vkr.ui.components.copyImageToInternalStorage
import kotlinx.coroutines.flow.firstOrNull

class UserRepository(private val userDao: UserDao, private val teamDao: TeamDao, private val session: UserSessionManager) {

    suspend fun getUserProfile(): UserProfileData? {
        val phone = session.userPhone.firstOrNull() ?: return null
        val user = userDao.getUserByPhone(phone) ?: return null

        val achievements = userDao.getUserWithAchievements(user.id)
            .firstOrNull()?.achievements.orEmpty()

        val events = userDao.getUserWithEvents(user.id)
            .firstOrNull()?.events.orEmpty()

        val team = user.teamId?.let { teamId ->
            teamDao.getAllTeams().firstOrNull { it.id == teamId }
        }
        return UserProfileData(user = user, team = team, achievements = achievements, events = events
        )
    }

    suspend fun loadUserFromApi(phone: String): UserEntity? {
        return try {
            val response = RetrofitInstance.api.getUserByPhone(phone)
            if (response.isSuccessful) {
                response.body()?.toEntity()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    suspend fun updateUser(user: UserEntity, fullName: String, nickname: String, newAvatarUri: Uri?, context: Context): Boolean {
        val avatarPath = newAvatarUri?.let { copyImageToInternalStorage(context, it) } ?: user.avatarUri
        val updatedDto = UserDTO(
            id = user.id,
            name = fullName,
            nickname = nickname,
            phone = user.phone,
            role = user.role,
            points = user.points,
            eventCount = user.eventCount,
            avatarUri = avatarPath,
            teamId = user.teamId
        )
        return try {
            val response = RetrofitInstance.userApi.updateUser(user.id, updatedDto)
            if (response.isSuccessful) {
                response.body()?.let { updated ->
                    userDao.updateUser(updated.toEntity())
                    session.saveUser(updated.phone, updated.role)
                    return true
                }
                false
            } else false
        } catch (e: Exception) {
            false
        }
    }
}

data class UserProfileData(
    val user: UserEntity,
    val team: TeamEntity?,
    val achievements: List<AchievementEntity>,
    val events: List<EventEntity>
)

