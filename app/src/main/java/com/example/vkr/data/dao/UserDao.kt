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
    @Upsert
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
    fun getUserWithEvents(userId: String): Flow<UserWithEvents>

    @Query("DELETE FROM user_event_cross_ref WHERE userId = :userId AND eventId = :eventId")
    suspend fun deleteUserEventCrossRef(userId: String, eventId: String)

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserWithAchievements(userId: String): Flow<UserWithAchievements>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAchievementCrossRef(crossRef: UserAchievementCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAchievementCrossRefs(crossRefs: List<UserAchievementCrossRef>)

    @Query("SELECT COUNT(*) FROM user_event_cross_ref WHERE eventId = :eventId")
    suspend fun getUserCountForEvent(eventId: String): Int

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserWithEventsOnce(userId: String): UserWithEvents?

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserWithAchievementsOnce(userId: Int): UserWithAchievements?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserEventCrossRefs(crossRefs: List<UserEventCrossRef>)
}
