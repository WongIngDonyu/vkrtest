package com.example.vkr.presentation.screens.teamdetail

import com.example.vkr.data.model.EventEntity
import com.example.vkr.data.model.TeamEntity
import com.example.vkr.data.model.UserEntity

interface TeamDetailContract {
    data class ViewState(
        val team: TeamEntity? = null,
        val users: List<UserEntity> = emptyList(),
        val events: List<EventEntity> = emptyList(),
        val currentUser: UserEntity? = null
    )

    interface View {
        fun updateState(state: ViewState)
        fun navigateToManageEvent(eventId: Int)
        fun goBack()
    }

    interface Presenter {
        fun onInit(teamId: Int)
        fun onJoinTeam()
        fun onLeaveTeam()
        fun onEventClicked(eventId: Int)
        fun onBackClicked()
    }
}

