package com.example.vkr.presentation.screens.profile

import com.example.vkr.data.model.AchievementEntity
import com.example.vkr.data.model.EventEntity
import com.example.vkr.data.model.TeamEntity
import com.example.vkr.data.model.UserEntity
import kotlinx.coroutines.flow.StateFlow

interface ProfileContract {
    data class ViewState(
        val user: UserEntity? = null,
        val team: TeamEntity? = null,
        val achievements: List<AchievementEntity> = emptyList(),
        val events: List<EventEntity> = emptyList(),
        val selectedDateFilter: String = "Предстоящие",
        val selectedEvent: EventEntity? = null
    )

    interface View {
        fun navigateToEditProfile()
        fun navigateToSettings()
    }

    interface Presenter {
        val state: StateFlow<ViewState>
        fun onEventSelected(event: EventEntity)
        fun onDateFilterSelected(filter: String)
        fun onEditProfileClicked()
        fun onSettingsClicked()
        fun onDialogDismissed()
        fun onInit()
    }
}
