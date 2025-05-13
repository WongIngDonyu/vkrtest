package com.example.vkr.presentation.screens.events

import android.app.Application
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
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
    navController: NavController,
    viewModel: EventsViewModel = viewModel()
) {
    val events = viewModel.filteredEvents
    val joinedEvents = viewModel.joinedEvents
    val organizedEvents = viewModel.organizedEvents
    val selectedEvent = viewModel.selectedEvent
    val searchQuery = viewModel.searchQuery
    val selectedFilter = viewModel.selectedFilter
    val isOrganizer = viewModel.isOrganizer
    val scope = rememberCoroutineScope()

    // 👇 Объединённый список участия
    val allParticipantEvents = (joinedEvents + organizedEvents)
        .distinctBy { it.id }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Мероприятия", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchChanged,
            placeholder = { Text("Поиск мероприятий") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(Modifier.height(12.dp))

        val filters = listOf("Все", "Сегодня", "На неделе", "В этом месяце")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            filters.forEach { label ->
                val isSelected = selectedFilter == label
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF7A5EFF) else Color(0xFFF2EBFF))
                        .clickable { viewModel.onFilterSelected(label) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else Color(0xFF7A5EFF),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 🔹 Все мероприятия
        Text("Все мероприятия", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        if (events.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(events) { event ->
                    val isJoined = joinedEvents.any { it.id == event.id }
                    val isUserOrganizer = organizedEvents.any { it.id == event.id }
                    val canJoin = !isJoined && event.creatorId != viewModel.currentUserId

                    val painter = if (!event.imageUri.isNullOrBlank()) {
                        rememberAsyncImagePainter(Uri.parse(event.imageUri))
                    } else {
                        painterResource(id = R.drawable.testew)
                    }

                    EventCardItem(
                        event = event,
                        painter = painter,
                        onClick = { viewModel.onEventClick(event) },
                        onJoin = {
                            viewModel.joinEvent(event.id) {
                                scope.launch { snackbarHostState.showSnackbar(it) }
                            }
                        },
                        showJoinButton = canJoin,
                        modifier = Modifier.width(180.dp)
                    )
                }
            }
        } else {
            Text(
                text = "Нет мероприятий по выбранному фильтру",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // 🔹 Вы участвуете
        Text("Вы участвуете", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        if (allParticipantEvents.isNotEmpty()) {
            allParticipantEvents.forEach { event ->
                MyEventItem(
                    event = event,
                    onClick = { viewModel.onEventClick(event) },
                    onDelete = {
                        if (event.creatorId != viewModel.currentUserId) {
                            viewModel.leaveEvent(event.id) {
                                scope.launch { snackbarHostState.showSnackbar(it) }
                            }
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))
            }
        } else {
            Text(
                text = "Вы пока не участвуете в мероприятиях",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // 🔹 Твои мероприятия (только для организатора)
        if (isOrganizer) {
            Text("Твои мероприятия", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (organizedEvents.isNotEmpty()) {
                organizedEvents.forEach { event ->
                    MyEventItem(
                        event = event,
                        onClick = { viewModel.onEventClick(event) },
                        onDelete = null // нельзя выйти из своих мероприятий
                    )
                    Spacer(Modifier.height(8.dp))
                }
            } else {
                Text(
                    text = "У вас пока нет созданных мероприятий",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }

    // 🔹 Диалог с описанием мероприятия
    selectedEvent?.let { event ->
        AlertDialog(
            onDismissRequest = viewModel::onDialogDismiss,
            confirmButton = {
                TextButton(onClick = viewModel::onDialogDismiss) {
                    Text("ОК")
                }
            },
            title = { Text(event.title, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    Text("📍 ${event.locationName}")
                    Text("🗓 ${event.dateTime}")
                    Spacer(Modifier.height(8.dp))
                    Text(event.description)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}



@Composable
fun EventCardItem(
    event: EventEntity,
    painter: Painter, // 🔁 добавлено
    onClick: () -> Unit,
    onJoin: () -> Unit,
    showJoinButton: Boolean,
    modifier: Modifier = Modifier
) {
    val isFinished = event.isFinished
    val textColor = if (isFinished) Color.Gray else Color.Unspecified

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !isFinished) { onClick() }
            .padding(8.dp)
    ) {
        Image(
            painter = painter, // 🔁 теперь используется готовый painter
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                Text(
                    text = event.locationName,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor
                )
            }

            if (showJoinButton && !isFinished) {
                IconButton(
                    onClick = onJoin,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Присоединиться",
                        tint = Color(0xFF7A5EFF)
                    )
                }
            }
        }

        if (isFinished) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Завершено",
                color = Color.Red,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}