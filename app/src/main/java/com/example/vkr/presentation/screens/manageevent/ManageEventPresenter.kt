package com.example.vkr.presentation.screens.manageevent

import android.net.Uri
import com.example.vkr.data.dao.EventDao
import com.example.vkr.data.dao.TeamDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageEventPresenter(
    private val view: ManageEventContract.View,
    private val eventDao: EventDao,
    private val teamDao: TeamDao
) : ManageEventContract.Presenter {

    private var state = ManageEventContract.ViewState()

    override fun onInit(eventId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val event = eventDao.getEventById(eventId)
            val teamName = event?.teamId?.let { id ->
                teamDao.getAllTeams().firstOrNull { it.id == id }?.name ?: ""
            } ?: ""

            state = state.copy(event = event, teamName = teamName)

            withContext(Dispatchers.Main) {
                view.updateState(state)
            }
        }
    }

    fun onAddImageClicked() {
        view.openImagePicker()
    }

    override fun onImagePicked(uris: List<Uri>) {
        state = state.copy(photoUris = state.photoUris + uris)
        view.updateState(state)
    }

    override fun onFinishEvent() {
        val event = state.event ?: return
        CoroutineScope(Dispatchers.IO).launch {
            eventDao.updateEvent(event.copy(isFinished = true))
            withContext(Dispatchers.Main) {
                view.goBack()
            }
        }
    }

    override fun onBackClicked() {
        view.goBack()
    }
}
