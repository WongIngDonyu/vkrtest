package com.example.vkr.presentation.screens.events

import com.example.vkr.data.dao.EventDao
import com.example.vkr.data.dao.UserDao
import com.example.vkr.data.model.EventEntity
import com.example.vkr.data.model.UserEntity
import com.example.vkr.data.model.UserEventCrossRef
import com.example.vkr.data.session.UserSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
class EventsPresenter(
    private val eventDao: EventDao,
    private val userDao: UserDao,
    private val session: UserSessionManager
) : EventsContract.Presenter {

    private var view: EventsContract.View? = null
    private var userId: Int? = null

    override fun attachView(view: EventsContract.View) {
        this.view = view
        runBlocking {
            val phone = session.userPhone.first()
            userId = phone?.let { userDao.getUserByPhone(it)?.id }
        }
    }

    override fun loadEvents() {
        CoroutineScope(Dispatchers.IO).launch {
            eventDao.getAllEvents().collect { events ->
                withContext(Dispatchers.Main) {
                    view?.showEvents(events)
                }
            }
        }
    }

    override fun onEventSelected(event: EventEntity) {
        view?.navigateToEventDetails(event)
    }

    override fun joinEvent(eventId: Int) {
        userId?.let { uid ->
            CoroutineScope(Dispatchers.IO).launch {
                userDao.insertUserEventCrossRef(UserEventCrossRef(uid, eventId))
                withContext(Dispatchers.Main) {
                    view?.showSnackbar("Вы успешно присоединились!")
                }
            }
        }
    }

    override fun leaveEvent(eventId: Int) {
        userId?.let { uid ->
            CoroutineScope(Dispatchers.IO).launch {
                userDao.deleteUserEventCrossRef(uid, eventId)
                withContext(Dispatchers.Main) {
                    view?.showSnackbar("Вы покинули мероприятие!")
                }
            }
        }
    }
}