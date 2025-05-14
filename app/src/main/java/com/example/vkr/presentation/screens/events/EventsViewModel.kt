package com.example.vkr.presentation.screens.events

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr.data.AppDatabase
import com.example.vkr.data.model.EventEntity
import com.example.vkr.data.model.UserEventCrossRef
import com.example.vkr.data.remote.RetrofitInstance
import com.example.vkr.data.session.UserSessionManager
import com.example.vkr.ui.components.DateTimeUtils
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate

class EventsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val eventDao = AppDatabase.getInstance(context).eventDao()
    private val userDao = AppDatabase.getInstance(context).userDao()
    private val session = UserSessionManager(context)
    private var userId: String? = null

    val currentUserId: String?
        get() = userId

    var allEvents by mutableStateOf<List<EventEntity>>(emptyList())
        private set

    var otherEvents by mutableStateOf<List<EventEntity>>(emptyList())
        private set

    var selectedEvent by mutableStateOf<EventEntity?>(null)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var selectedFilter by mutableStateOf("Все")
        private set

    var isOrganizer by mutableStateOf(false)
        private set

    var joinedEvents by mutableStateOf<List<EventEntity>>(emptyList())
        private set

    var organizedEvents by mutableStateOf<List<EventEntity>>(emptyList())
        private set

    var filteredEvents by mutableStateOf<List<EventEntity>>(emptyList())
        private set

    private val eventApi = RetrofitInstance.eventApi


    init {
        viewModelScope.launch {
            val phone = session.userPhone.firstOrNull()
            val user = phone?.let { userDao.getUserByPhone(it) }
            userId = user?.id
            isOrganizer = user?.role == "ORGANIZER"
            eventDao.getAllEvents().collect { events ->
                allEvents = events
                applyFilters()
            }
        }
    }

    fun onSearchChanged(query: String) {
        searchQuery = query
        applyFilters()
    }

    fun onFilterSelected(filter: String) {
        selectedFilter = filter
        applyFilters()
    }

    fun onEventClick(event: EventEntity) {
        selectedEvent = event
    }

    fun onDialogDismiss() {
        selectedEvent = null
    }

    fun joinEvent(eventId: String, onSnackbar: (String) -> Unit) {
        if (userId == null) return

        viewModelScope.launch {
            val event = allEvents.find { it.id == eventId } ?: return@launch
            val uid = userId!!

            if (event.creatorId == uid || joinedEvents.any { it.id == eventId }) return@launch

            try {
                val response = eventApi.joinEvent(eventId, uid)
                if (response.isSuccessful || response.code() == 204) {
                    userDao.insertUserEventCrossRef(UserEventCrossRef(uid, eventId))
                    joinedEvents = joinedEvents + event
                    otherEvents = otherEvents - event
                    onSnackbar("Вы присоединились к мероприятию!")
                    applyFilters()
                } else {
                    onSnackbar("Ошибка: ${response.code()}")
                }
            } catch (e: Exception) {
                onSnackbar("Ошибка подключения: ${e.localizedMessage}")
            }
        }
    }

    fun leaveEvent(eventId: String, onSnackbar: (String) -> Unit) {
        if (userId == null) return
        viewModelScope.launch {
            val event = allEvents.find { it.id == eventId } ?: return@launch
            val uid = userId!!
            if (event.creatorId == uid || event.isFinished) return@launch
            try {
                val response = eventApi.leaveEvent(eventId, uid)
                if (response.isSuccessful || response.code() == 204) {
                    userDao.deleteUserEventCrossRef(uid, eventId)
                    joinedEvents = joinedEvents - event
                    otherEvents = otherEvents + event
                    onSnackbar("Вы покинули мероприятие!")
                    applyFilters()
                } else {
                    onSnackbar("Ошибка: ${response.code()}")
                }
            } catch (e: Exception) {
                onSnackbar("Ошибка подключения: ${e.localizedMessage}")
            }
        }
    }

    private fun applyFilters() {
        val now = LocalDate.now()
        val filtered = allEvents.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }
        val filteredByTime = when (selectedFilter) {
            "Сегодня" -> filtered.filter {
                DateTimeUtils.parseDisplayFormatted(it.dateTime)?.toLocalDate() == now
            }
            "На неделе" -> filtered.filter {
                val date = DateTimeUtils.parseDisplayFormatted(it.dateTime)?.toLocalDate()
                date != null && date.isAfter(now.minusDays(1)) && date.isBefore(now.plusDays(7))
            }
            "В этом месяце" -> filtered.filter {
                DateTimeUtils.parseDisplayFormatted(it.dateTime)?.month == now.month
            }
            else -> filtered
        }
        filteredEvents = filteredByTime
        viewModelScope.launch {
            val uid = userId ?: return@launch
            val userEventIds = userDao.getUserWithEventsOnce(uid)?.events?.map { it.id }?.toSet() ?: emptySet()
            joinedEvents = filteredByTime.filter { it.id in userEventIds && it.creatorId != uid }
            organizedEvents = filteredByTime.filter { it.creatorId == uid }
            otherEvents = filteredByTime.filter {
                it.id !in userEventIds && it.creatorId != uid
            }
        }
    }
}