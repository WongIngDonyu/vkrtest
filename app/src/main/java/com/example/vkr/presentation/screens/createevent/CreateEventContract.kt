package com.example.vkr.presentation.screens.createevent

import android.net.Uri
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalTime

interface CreateEventContract {
    data class ViewState(
        val title: String = "",
        val description: String = "",
        val location: String = "",
        val selectedDate: LocalDate? = null,
        val selectedTime: LocalTime? = null,
        val imageUri: Uri? = null,
        val showDatePicker: Boolean = false,
        val showTimePicker: Boolean = false,
        val titleError: Boolean = false,
        val selectedTeamId: Int? = null,
        val selectedTeamName: String? = null
    )

    interface View {
        fun openImagePicker()
        fun goBack()
    }

    interface Presenter {
        val state: StateFlow<ViewState>
        fun onInit()
        fun onTitleChange(value: String)
        fun onDescriptionChange(value: String)
        fun onLocationChange(value: String)
        fun onPickDate()
        fun onDismissDate()
        fun onDateSelected(millis: Long?)
        fun onPickTime()
        fun onDismissTime()
        fun onTimeSelected(hour: Int, minute: Int)
        fun onPickImage()
        fun onImagePicked(uri: Uri?)
        fun onCreateEvent()
        fun onCancel()
        fun onTeamSelected(teamId: Int)
    }
}