package com.example.vkr.data.dao

import androidx.room.*
import com.example.vkr.data.model.UserAchievementCrossRef
import com.example.vkr.data.model.UserEntity
import com.example.vkr.data.model.UserEventCrossRef
import com.example.vkr.data.model.UserWithAchievements
import com.example.vkr.data.model.UserWithEvents
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    fun observeUserByPhone(phone: String): Flow<UserEntity?>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserEventCrossRef(crossRef: UserEventCrossRef)

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserWithEvents(userId: Int): Flow<UserWithEvents>

    @Query("DELETE FROM UserEventCrossRef WHERE userId = :userId AND eventId = :eventId")
    suspend fun deleteUserEventCrossRef(userId: Int, eventId: Int)

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserWithAchievements(userId: Int): Flow<UserWithAchievements>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAchievementCrossRef(crossRef: UserAchievementCrossRef)

    @Query("SELECT COUNT(*) FROM UserEventCrossRef WHERE eventId = :eventId")
    suspend fun getUserCountForEvent(eventId: Int): Int

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserWithEventsOnce(userId: Int): UserWithEvents?

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserWithAchievementsOnce(userId: Int): UserWithAchievements?
}
