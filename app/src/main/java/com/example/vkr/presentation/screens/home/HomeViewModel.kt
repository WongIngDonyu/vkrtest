package com.example.vkr.presentation.screens.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr.data.dao.EventDao
import com.example.vkr.data.dao.UserDao
import com.example.vkr.ui.components.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.vkr.data.model.EventEntity
import com.example.vkr.data.remote.RetrofitInstance
import com.example.vkr.data.repository.EventRepository
import com.example.vkr.data.session.UserSessionManager
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate

class HomeViewModel(
    private val repository: EventRepository,
    private val session: UserSessionManager
) : ViewModel() {

    var state by mutableStateOf(HomeViewState())
        private set

    var selectedEvent by mutableStateOf<EventEntity?>(null)
        private set

    init {
        viewModelScope.launch {
            try {
                repository.fetchAndSaveEvents()
            } catch (e: Exception) {
                println("Ошибка получения событий с сервера: ${e.localizedMessage}")
            }
            loadEvents()
        }
    }

    fun loadEvents() {
        viewModelScope.launch {
            val phone = session.userPhone.firstOrNull()
            val user = phone?.let { repository.getUserByPhone(it) }
            val joinedEvents = user?.id?.let { repository.getJoinedEventsForUser(it) } ?: emptyList()
            val counts = repository.getParticipantCounts(joinedEvents)
            state = state.copy(
                allEvents = joinedEvents,
                participantCounts = counts
            )
            applyFilters()
        }
    }

    fun onSearchChanged(query: String) {
        state = state.copy(searchQuery = query)
        applyFilters()
    }

    fun onFilterSelected(filter: String) {
        state = state.copy(selectedFilter = filter)
        applyFilters()
    }

    fun onFavoriteToggle(event: EventEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(event)
            loadEvents()
        }
    }

    fun onEventClick(event: EventEntity) {
        selectedEvent = event
    }

    fun onDialogClose() {
        selectedEvent = null
    }

    private fun applyFilters() {
        viewModelScope.launch {
            val phone = session.userPhone.firstOrNull()
            val user = phone?.let { repository.getUserByPhone(it) }
            val joinedEvents = user?.id?.let { repository.getJoinedEventsForUser(it) } ?: emptyList()

            val filtered = when (state.selectedFilter) {
                "Предстоящие" -> {
                    val now = LocalDate.now()
                    val twoDaysLater = now.plusDays(2)
                    joinedEvents.filter {
                        val date = DateTimeUtils.parseDisplayFormatted(it.dateTime)?.toLocalDate()
                        date in now..twoDaysLater
                    }
                }
                "Популярные" -> joinedEvents.filter {
                    (state.participantCounts[it.id] ?: 0) >= 5
                }
                else -> joinedEvents
            }

            val searched = if (state.searchQuery.isNotBlank()) {
                filtered.filter {
                    it.title.contains(state.searchQuery.trim(), ignoreCase = true)
                }
            } else filtered

            state = state.copy(filteredEvents = searched, allEvents = joinedEvents)
        }
    }
}

data class HomeViewState(
    val allEvents: List<EventEntity> = emptyList(),
    val filteredEvents: List<EventEntity> = emptyList(),
    val participantCounts: Map<String, Int> = emptyMap(),
    val searchQuery: String = "",
    val selectedFilter: String = "Все"
)
