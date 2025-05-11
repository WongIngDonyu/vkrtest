package com.example.vkr.presentation.screens.teamdetail

import com.example.vkr.data.dao.EventDao
import com.example.vkr.data.dao.TeamDao
import com.example.vkr.data.dao.UserDao
import com.example.vkr.data.session.UserSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TeamDetailPresenter(
    private val view: TeamDetailContract.View,
    private val teamDao: TeamDao,
    private val userDao: UserDao,
    private val eventDao: EventDao,
    private val session: UserSessionManager
) : TeamDetailContract.Presenter {

    private var state = TeamDetailContract.ViewState()

    override fun onInit(teamId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val team = teamDao.getAllTeams().firstOrNull { it.id == teamId }
            val users = teamDao.getUsersByTeam(teamId)
            val events = eventDao.getEventsByTeam(teamId)
            val phone = session.userPhone.firstOrNull()
            val currentUser = phone?.let { userDao.getUserByPhone(it) }

            state = state.copy(
                team = team,
                users = users,
                events = events,
                currentUser = currentUser
            )

            withContext(Dispatchers.Main) {
                view.updateState(state)
            }
        }
    }

    override fun onJoinTeam() {
        CoroutineScope(Dispatchers.IO).launch {
            val currentUser = state.currentUser ?: return@launch
            val teamId = state.team?.id ?: return@launch
            teamDao.joinTeam(currentUser.id, teamId)
            refresh(teamId)
        }
    }

    override fun onLeaveTeam() {
        CoroutineScope(Dispatchers.IO).launch {
            val currentUser = state.currentUser ?: return@launch
            val teamId = state.team?.id ?: return@launch
            teamDao.leaveTeam(currentUser.id)
            refresh(teamId)
        }
    }

    override fun onEventClicked(eventId: Int) {
        view.navigateToManageEvent(eventId)
    }

    override fun onBackClicked() {
        view.goBack()
    }

    private fun refresh(teamId: Int) {
        onInit(teamId)
    }
}
