package com.example.vkr.presentation.screens.home

import com.example.vkr.data.model.EventEntity

interface HomeContract {
    data class ViewState(
        val allEvents: List<EventEntity> = emptyList(),
        val participantCounts: Map<Int, Int> = emptyMap(),
        val searchQuery: String = "",
        val selectedFilter: String = "Все"
    )

    interface View {
        fun updateState(state: ViewState)
        fun showEventDetails(event: EventEntity)
        fun hideEventDetails()
    }

    interface Presenter {
        fun onInit()
        fun onSearchChanged(query: String)
        fun onFilterSelected(filter: String)
        fun onFavoriteToggle(event: EventEntity)
        fun onEventClick(event: EventEntity)
        fun onDialogClose()
    }
}
