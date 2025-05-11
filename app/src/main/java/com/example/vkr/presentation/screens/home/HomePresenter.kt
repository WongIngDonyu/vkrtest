package com.example.vkr.presentation.screens.home

import com.example.vkr.data.dao.EventDao
import com.example.vkr.data.dao.UserDao
import com.example.vkr.data.model.EventEntity
import com.example.vkr.ui.components.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomePresenter(
    private val view: HomeContract.View,
    private val eventDao: EventDao,
    private val userDao: UserDao
) : HomeContract.Presenter {

    private var allEvents: List<EventEntity> = emptyList()
    private var participantCounts: Map<Int, Int> = emptyMap()
    private var currentFilter = "Все"
    private var currentSearch = ""

    override fun onInit() {
        CoroutineScope(Dispatchers.IO).launch {
            allEvents = eventDao.getAllEvents().first()
            val counts = allEvents.associate { it.id to userDao.getUserCountForEvent(it.id) }
            participantCounts = counts
            updateView()
        }
    }

    override fun onSearchChanged(query: String) {
        currentSearch = query
        updateView()
    }

    override fun onFilterSelected(filter: String) {
        currentFilter = filter
        updateView()
    }

    override fun onFavoriteToggle(event: EventEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            val updated = event.copy(isFavorite = !event.isFavorite)
            eventDao.updateEvent(updated)
            allEvents = eventDao.getAllEvents().first()
            updateView()
        }
    }

    override fun onEventClick(event: EventEntity) {
        view.showEventDetails(event)
    }

    override fun onDialogClose() {
        view.hideEventDetails()
    }

    private fun updateView() {
        val filtered = when (currentFilter) {
            "Предстоящие" -> {
                val now = LocalDate.now()
                val twoDaysLater = now.plusDays(2)
                allEvents.filter {
                    val date = DateTimeUtils.parseDisplayFormatted(it.dateTime)?.toLocalDate()
                    date in now..twoDaysLater
                }
            }
            "Популярные" -> allEvents.filter { participantCounts[it.id] ?: 0 >= 5 }
            else -> allEvents
        }

        val result = if (currentSearch.isNotBlank()) {
            filtered.filter {
                it.title.contains(currentSearch.trim(), ignoreCase = true)
            }
        } else filtered

        CoroutineScope(Dispatchers.Main).launch {
            view.updateState(
                HomeContract.ViewState(
                    allEvents = result,
                    participantCounts = participantCounts,
                    searchQuery = currentSearch,
                    selectedFilter = currentFilter
                )
            )
        }
    }
}
