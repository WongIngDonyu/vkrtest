package com.example.vkr.presentation.screens.events

import com.example.vkr.data.model.EventEntity
interface EventsContract {
    interface View {
        fun showEvents(events: List<EventEntity>)
        fun showSnackbar(message: String)
        fun navigateToEventDetails(event: EventEntity)
    }

    interface Presenter {
        fun attachView(view: View)
        fun loadEvents()
        fun onEventSelected(event: EventEntity)
        fun joinEvent(eventId: Int)
        fun leaveEvent(eventId: Int)
    }
}