package com.example.vkr.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


// Создаём DataStore-инстанс
private val Context.dataStore by preferencesDataStore(name = "user_session")

object UserSessionKeys {
    val PHONE = stringPreferencesKey("phone")
    val ROLE = stringPreferencesKey("role")
}

class UserSessionManager(private val context: Context) {

    val userPhone: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[UserSessionKeys.PHONE]
    }

    val userRole: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[UserSessionKeys.ROLE]
    }

    suspend fun saveUser(phone: String, role: String) {
        context.dataStore.edit { prefs ->
            prefs[UserSessionKeys.PHONE] = phone
            prefs[UserSessionKeys.ROLE] = role
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}