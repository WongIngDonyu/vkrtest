package com.example.vkr.presentation.screens.search

import com.example.vkr.data.dao.TeamDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchPresenter(
    private val view: SearchContract.View,
    private val teamDao: TeamDao
) : SearchContract.Presenter {

    private var state = SearchContract.ViewState()

    override fun onInit() {
        CoroutineScope(Dispatchers.IO).launch {
            teamDao.getAllTeamsFlow().collect { teams ->
                withContext(Dispatchers.Main) {
                    state = state.copy(teams = teams)
                    view.updateState(state)
                }
            }
        }
    }

    override fun onTeamClicked(teamId: Int) {
        view.navigateToTeamDetail(teamId)
    }

    override fun onShowDialog() {
        state = state.copy(showDialog = true)
        view.updateState(state)
    }

    override fun onHideDialog() {
        state = state.copy(showDialog = false)
        view.updateState(state)
    }
}
