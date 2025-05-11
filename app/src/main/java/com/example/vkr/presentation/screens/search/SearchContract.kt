package com.example.vkr.presentation.screens.search

import com.example.vkr.data.model.TeamEntity

interface SearchContract {
    data class ViewState(
        val teams: List<TeamEntity> = emptyList(),
        val showDialog: Boolean = false
    )

    interface View {
        fun updateState(state: ViewState)
        fun navigateToTeamDetail(teamId: Int)
    }

    interface Presenter {
        fun onInit()
        fun onTeamClicked(teamId: Int)
        fun onShowDialog()
        fun onHideDialog()
    }
}
