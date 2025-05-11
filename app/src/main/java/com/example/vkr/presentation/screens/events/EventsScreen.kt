package com.example.vkr.presentation.screens.events

import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.vkr.R
import com.example.vkr.data.AppDatabase
import com.example.vkr.data.model.EventEntity
import com.example.vkr.data.model.UserEntity
import com.example.vkr.data.model.UserEventCrossRef
import com.example.vkr.data.session.UserSessionManager
import com.example.vkr.presentation.components.MyEventItem
import com.example.vkr.ui.components.DateTimeUtils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
@Composable
fun EventsScreen(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    presenter: EventsContract.Presenter
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val events = remember { mutableStateListOf<EventEntity>() }
    val selectedEvent = remember { mutableStateOf<EventEntity?>(null) }

    // "View" реализация
    val view = object : EventsContract.View {
        override fun showEvents(eventsList: List<EventEntity>) {
            events.clear()
            events.addAll(eventsList)
        }

        override fun showSnackbar(message: String) {
            scope.launch {
                snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
            }
        }

        override fun navigateToEventDetails(event: EventEntity) {
            selectedEvent.value = event
        }
    }

    // Привязка view к презентеру
    LaunchedEffect(Unit) {
        if (presenter is EventsPresenter) {
            presenter.attachView(view)
            presenter.loadEvents()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Мероприятия", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            events.forEach { event ->
                MyEventItem(
                    event = event,
                    onDelete = { presenter.leaveEvent(event.id) },
                    onClick = { presenter.onEventSelected(event) }
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    selectedEvent.value?.let { event ->
        AlertDialog(
            onDismissRequest = { selectedEvent.value = null },
            confirmButton = {
                TextButton(onClick = { selectedEvent.value = null }) {
                    Text("ОК")
                }
            },
            title = { Text(event.title, style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column {
                    Text("Место: ${event.locationName}", style = MaterialTheme.typography.bodyLarge)
                    Text("Время: ${event.dateTime}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(event.description, style = MaterialTheme.typography.bodySmall)
                }
            },
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp
        )
    }
}