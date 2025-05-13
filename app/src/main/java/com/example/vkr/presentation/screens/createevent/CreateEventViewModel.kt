package com.example.vkr.presentation.screens.createevent

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vkr.data.AppDatabase
import com.example.vkr.data.model.EventEntity
import com.example.vkr.data.model.EventRequestDTO
import com.example.vkr.data.model.UserEventCrossRef
import com.example.vkr.data.remote.RetrofitInstance
import com.example.vkr.data.session.UserSessionManager
import com.example.vkr.ui.components.DateTimeUtils
import com.example.vkr.ui.components.copyImageToInternalStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

class CreateEventViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext
    private val db = AppDatabase.getInstance(context)
    private val userDao = db.userDao()
    private val teamDao = db.teamDao()
    private val eventDao = db.eventDao()
    private val session = UserSessionManager(context)

    var state by mutableStateOf(CreateEventUiState())
        private set

    fun onTitleChange(value: String) {
        state = state.copy(title = value, titleError = false)
    }

    fun onDescriptionChange(value: String) {
        state = state.copy(description = value)
    }

    fun onLocationChange(value: String) {
        state = state.copy(location = value)
    }

    fun onPickDate() {
        state = state.copy(showDatePicker = true)
    }

    fun onDismissDate() {
        state = state.copy(showDatePicker = false)
    }

    fun onDateSelected(millis: Long?) {
        val date = millis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }
        state = state.copy(selectedDate = date, showDatePicker = false)
    }

    fun onPickTime() {
        state = state.copy(showTimePicker = true)
    }

    fun onDismissTime() {
        state = state.copy(showTimePicker = false)
    }

    fun onTimeSelected(hour: Int, minute: Int) {
        val time = LocalTime.of(hour, minute)
        state = state.copy(selectedTime = time, showTimePicker = false)
    }

    fun onImagePicked(uri: Uri?) {
        state = state.copy(imageUri = uri)
    }

    fun onTeamSelected(teamId: String) {
        viewModelScope.launch {
            val team = teamDao.getAllTeams().firstOrNull { it.id == teamId }
            state = state.copy(
                selectedTeamId = team?.id,
                selectedTeamName = team?.name
            )
        }
    }

    fun onCreateEvent(onSuccess: () -> Unit) {
        val st = state
        val title = st.title.trim()
        if (title.isBlank()) {
            state = st.copy(titleError = true)
            return
        }

        val date = st.selectedDate ?: return
        val time = st.selectedTime ?: return
        val dateTime = LocalDateTime.of(date, time)
        val formattedDateTime = DateTimeUtils.formatDisplay(dateTime)

        viewModelScope.launch {
            val phone = session.userPhone.first() ?: return@launch
            val user = userDao.getUserByPhone(phone) ?: return@launch

            val imagePath = st.imageUri?.let { copyImageToInternalStorage(context, it) }

            val fallbackTeamId: String? = user.teamId
            val finalTeamId = st.selectedTeamId ?: fallbackTeamId

            val dto = EventRequestDTO(
                title = title,
                description = st.description,
                locationName = st.location,
                latitude = 55.0,
                longitude = 37.0,
                dateTime = formattedDateTime,
                creatorId = user.id,
                teamId = finalTeamId, // ⬅ здесь ID
                imageUri = listOfNotNull(imagePath)
            )

            try {
                val response = RetrofitInstance.eventApi.createEvent(dto)
                if (response.isSuccessful) {
                    val serverEvent = response.body()
                    if (serverEvent != null) {
                        val localEvent = EventEntity(
                            id = serverEvent.id,
                            title = serverEvent.title,
                            description = serverEvent.description,
                            locationName = serverEvent.locationName,
                            latitude = serverEvent.latitude,
                            longitude = serverEvent.longitude,
                            dateTime = serverEvent.dateTime,
                            creatorId = serverEvent.creatorId,
                            teamId = serverEvent.teamId, // ⬅ сохрани ID
                            imageUri = serverEvent.imageUri.firstOrNull(),
                            isFinished = serverEvent.finished
                        )
                        eventDao.insertEvent(localEvent)
                        userDao.insertUserEventCrossRef(UserEventCrossRef(user.id, localEvent.id))
                    }
                } else {
                    println("❗ Ошибка при отправке события: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                println("❗ Ошибка подключения при отправке события: ${e.localizedMessage}")
            }

            onSuccess()
        }
    }
}
data class CreateEventUiState(
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val selectedDate: LocalDate? = null,
    val selectedTime: LocalTime? = null,
    val imageUri: Uri? = null,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val titleError: Boolean = false,
    val selectedTeamId: String? = null,
    val selectedTeamName: String? = null
)

class CreateEventViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateEventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateEventViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}