package com.example.vkr.presentation.screens.createevent

import android.content.Context
import android.net.Uri
import androidx.navigation.NavController
import com.example.vkr.data.AppDatabase
import com.example.vkr.data.model.EventEntity
import com.example.vkr.data.model.UserEventCrossRef
import com.example.vkr.data.session.UserSessionManager
import com.example.vkr.ui.components.DateTimeUtils
import com.example.vkr.ui.components.copyImageToInternalStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class CreateEventPresenter(
    private val context: Context,
    private val navController: NavController,
    private val view: CreateEventContract.View
) : CreateEventContract.Presenter {

    private val _state = MutableStateFlow(CreateEventContract.ViewState())
    override val state: StateFlow<CreateEventContract.ViewState> = _state

    private val db = AppDatabase.getInstance(context)
    private val userDao = db.userDao()
    private val teamDao = db.teamDao()
    private val eventDao = db.eventDao()
    private val session = UserSessionManager(context)

    override fun onInit() {}

    override fun onTitleChange(value: String) {
        _state.value = _state.value.copy(title = value, titleError = false)
    }

    override fun onDescriptionChange(value: String) {
        _state.value = _state.value.copy(description = value)
    }

    override fun onLocationChange(value: String) {
        _state.value = _state.value.copy(location = value)
    }

    override fun onPickDate() {
        _state.value = _state.value.copy(showDatePicker = true)
    }

    override fun onDismissDate() {
        _state.value = _state.value.copy(showDatePicker = false)
    }

    override fun onDateSelected(millis: Long?) {
        val date = millis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }
        _state.value = _state.value.copy(selectedDate = date, showDatePicker = false)
    }

    override fun onPickTime() {
        _state.value = _state.value.copy(showTimePicker = true)
    }

    override fun onDismissTime() {
        _state.value = _state.value.copy(showTimePicker = false)
    }

    override fun onTimeSelected(hour: Int, minute: Int) {
        val time = LocalTime.of(hour, minute)
        _state.value = _state.value.copy(selectedTime = time, showTimePicker = false)
    }

    override fun onPickImage() {
        view.openImagePicker()
    }

    override fun onImagePicked(uri: Uri?) {
        _state.value = _state.value.copy(imageUri = uri)
    }

    override fun onCreateEvent() {
        val st = _state.value
        val title = st.title.trim()
        if (title.isBlank()) {
            _state.value = st.copy(titleError = true)
            return
        }
        val date = st.selectedDate ?: return
        val time = st.selectedTime ?: return

        val dateTime = LocalDateTime.of(date, time)
        val formattedDateTime = DateTimeUtils.formatDisplay(dateTime)

        CoroutineScope(Dispatchers.IO).launch {
            val phone = session.userPhone.first() ?: return@launch
            val user = userDao.getUserByPhone(phone) ?: return@launch

            val imagePath = st.imageUri?.let { copyImageToInternalStorage(context, it) }

            val event = EventEntity(
                title = title,
                description = st.description,
                locationName = st.location,
                latitude = 55.0,
                longitude = 37.0,
                dateTime = formattedDateTime,
                creatorId = user.id,
                teamId = user.teamId ?: 1,
                imageUri = imagePath
            )

            val id = eventDao.insertEvent(event)
            userDao.insertUserEventCrossRef(UserEventCrossRef(user.id, id.toInt()))

            withContext(Dispatchers.Main) {
                view.goBack()
            }
        }
    }

    override fun onCancel() {
        view.goBack()
    }

    override fun onTeamSelected(teamId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val team = teamDao.getAllTeams().firstOrNull { it.id == teamId }
            withContext(Dispatchers.Main) {
                _state.update {
                    it.copy(
                        selectedTeamId = team?.id,
                        selectedTeamName = team?.name
                    )
                }
            }
        }
    }
}
