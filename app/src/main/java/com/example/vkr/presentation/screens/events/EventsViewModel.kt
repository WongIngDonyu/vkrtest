package com.example.vkr.presentation.screens.events

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr.data.AppDatabase
import com.example.vkr.data.dao.EventDao
import com.example.vkr.data.dao.UserDao
import com.example.vkr.data.model.EventEntity
import com.example.vkr.data.model.UserEventCrossRef
import com.example.vkr.data.session.UserSessionManager
import com.example.vkr.ui.components.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class EventsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val eventDao = AppDatabase.getInstance(context).eventDao()
    private val userDao = AppDatabase.getInstance(context).userDao()
    private val session = UserSessionManager(context)

    val currentUserId: Int?
        get() = userId

    var allEvents by mutableStateOf<List<EventEntity>>(emptyList())
        private set

    var myEvents by mutableStateOf<List<EventEntity>>(emptyList())
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

    private var userId: Int? = null

    init {
        viewModelScope.launch {
            val phone = session.userPhone.firstOrNull()
            val user = phone?.let { userDao.getUserByPhone(it) }
            userId = user?.id
            isOrganizer = user?.role == "Организатор"

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

    fun joinEvent(eventId: Int, onSnackbar: (String) -> Unit) {
        if (userId == null) return

        viewModelScope.launch {
            val event = allEvents.find { it.id == eventId } ?: return@launch

            // Нельзя присоединиться к мероприятию, если ты его организатор или уже присоединился
            if (event.creatorId == userId || myEvents.any { it.id == eventId }) return@launch

            userDao.insertUserEventCrossRef(UserEventCrossRef(userId!!, eventId))
            onSnackbar("Вы присоединились к мероприятию!")

            // Обновим события вручную, чтобы сразу отобразить изменения
            applyFilters()
        }
    }

    fun leaveEvent(eventId: Int, onSnackbar: (String) -> Unit) {
        if (userId == null) return

        viewModelScope.launch {
            val event = allEvents.find { it.id == eventId } ?: return@launch

            // Организатор не может покинуть своё мероприятие или завершённое
            if (event.creatorId == userId || event.isFinished) return@launch

            userDao.deleteUserEventCrossRef(userId!!, eventId)
            onSnackbar("Вы покинули мероприятие!")
            applyFilters()
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

        filteredEvents = filteredByTime // ✅ Это снаружи launch

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