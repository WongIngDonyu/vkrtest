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
import com.example.vkr.data.repository.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageEventViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EventRepository

    var event by mutableStateOf<EventEntity?>(null)
        private set

    var teamName by mutableStateOf("")
        private set

    var photoUris by mutableStateOf<List<Uri>>(emptyList())
        private set

    init {
        val context = application.applicationContext
        repository = EventRepository(api = RetrofitInstance.eventApi, eventDao = AppDatabase.getInstance(context).eventDao(), teamDao = AppDatabase.getInstance(context).teamDao(), userDao = AppDatabase.getInstance(context).userDao())
    }

    fun loadEvent(eventId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val (loadedEvent, loadedTeamName) = repository.getEventWithTeamName(eventId)
            withContext(Dispatchers.Main) {
                event = loadedEvent
                teamName = loadedTeamName
            }
        }
    }

    fun onImagePicked(uris: List<Uri>) {
        photoUris = photoUris + uris
    }

    fun finishEvent(onSuccess: () -> Unit) {
        val currentEvent = event ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.finishEvent(currentEvent)
            if (success) {
                val updated = currentEvent.copy(isFinished = true)
                withContext(Dispatchers.Main) {
                    event = updated
                    onSuccess()
                }
            }
        }
    }
}
