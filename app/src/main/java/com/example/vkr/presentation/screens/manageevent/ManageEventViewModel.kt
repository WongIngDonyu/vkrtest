package com.example.vkr.presentation.screens.manageevent

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr.data.AppDatabase
import com.example.vkr.data.model.EventEntity
import com.example.vkr.data.remote.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageEventViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val eventDao = AppDatabase.getInstance(context).eventDao()
    private val teamDao = AppDatabase.getInstance(context).teamDao()
    private val eventApi = RetrofitInstance.eventApi // добавь это

    var event by mutableStateOf<EventEntity?>(null)
        private set

    var teamName by mutableStateOf("")
        private set

    var photoUris by mutableStateOf<List<Uri>>(emptyList())
        private set

    fun loadEvent(eventId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val loadedEvent = eventDao.getEventById(eventId)
            val team = loadedEvent?.teamId?.let { id ->
                teamDao.getAllTeams().firstOrNull { it.id == id }?.name ?: ""
            } ?: ""

            withContext(Dispatchers.Main) {
                event = loadedEvent
                teamName = team
            }
        }
    }

    fun onImagePicked(uris: List<Uri>) {
        photoUris = photoUris + uris
    }

    fun finishEvent(onSuccess: () -> Unit) {
        val e = event ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = eventApi.finishEvent(e.id)
                if (response.isSuccessful) {
                    val updated = e.copy(isFinished = true)
                    eventDao.updateEvent(updated)

                    withContext(Dispatchers.Main) {
                        event = updated // обновим UI
                        onSuccess()
                    }
                } else {
                    println("❗ Ошибка завершения события: ${response.code()}")
                }
            } catch (ex: Exception) {
                println("❗ Ошибка подключения: ${ex.localizedMessage}")
            }
        }
    }
}
