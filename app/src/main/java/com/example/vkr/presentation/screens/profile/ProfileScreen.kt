package com.example.vkr.presentation.screens.profile

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import com.example.vkr.R
import com.example.vkr.presentation.components.AchievementCard
import com.example.vkr.presentation.components.EventCard2
import com.example.vkr.presentation.components.StatCard
import com.example.vkr.ui.components.DateTimeUtils
import java.time.LocalDateTime

@Composable
fun ProfileScreen(navController: NavController, modifier: Modifier = Modifier, viewModel: ProfileViewModel = viewModel()) {
    val user = viewModel.user
    val team = viewModel.team
    val achievements = viewModel.achievements
    val events = viewModel.events
    val selectedEvent = viewModel.selectedEvent
    val selectedDateFilter = viewModel.selectedDateFilter
    val currentBackStackEntry = navController.currentBackStackEntryAsState().value

    LaunchedEffect(currentBackStackEntry) {
        val shouldReload = currentBackStackEntry?.savedStateHandle?.get<Boolean>("reloadProfile") ?: false
        if (shouldReload) {
            viewModel.loadProfileFromDb()
            if (currentBackStackEntry != null) {
                currentBackStackEntry.savedStateHandle["reloadProfile"] = false
            }
        }
    }
    val filteredEvents = events.filter { event ->
        val date = DateTimeUtils.parseDisplayFormatted(event.dateTime)
        when (selectedDateFilter) {
            "Предстоящие" -> !event.isFinished && date?.isAfter(LocalDateTime.now()) == true
            "Прошедшие" -> event.isFinished || (date?.isBefore(LocalDateTime.now()) == true)
            else -> true
        }
    }
    val avatarPainter = if (!user?.avatarUri.isNullOrBlank()) {
        rememberAsyncImagePainter(Uri.parse(user?.avatarUri))
    } else {
        painterResource(R.drawable.images)
    }
    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = avatarPainter,
                contentDescription = "Аватар",
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(user?.name ?: "Имя", style = MaterialTheme.typography.titleLarge)
                Text("@${user?.nickname ?: "ник"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(user?.role ?: "роль", style = MaterialTheme.typography.bodyMedium)
                Text(
                    team?.name?.let { "В команде: $it" } ?: "Без команды",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Достижения", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(achievements) {
                AchievementCard(it.title, it.description, it.imageResId)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Очков", user?.points?.toString() ?: "0", Icons.Default.Star)
            StatCard("Мероприятий", user?.eventCount?.toString() ?: "0", Icons.Default.Event)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Мои мероприятия", style = MaterialTheme.typography.titleMedium)
        if (filteredEvents.isEmpty()) {
            Text("Нет мероприятий по выбранному фильтру.", color = Color.Gray)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredEvents) { event ->
                    val painter = if (!event.imageUri.isNullOrBlank()) {
                        rememberAsyncImagePainter(Uri.parse(event.imageUri))
                    } else {
                        painterResource(id = R.drawable.images)
                    }

                    EventCard2(title = event.title, datePlace = event.dateTime, painter = painter) {
                        viewModel.selectEvent(event)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("Предстоящие", "Прошедшие").forEach { label ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                    viewModel.selectDateFilter(label)
                }) {
                    Icon(
                        imageVector = if (label == "Предстоящие") Icons.Default.CalendarToday else Icons.Default.History,
                        contentDescription = label,
                        tint = if (selectedDateFilter == label) Color(0xFF7A5EFF) else Color.Gray
                    )
                    Text(label, style = MaterialTheme.typography.labelMedium.copy(color = if (selectedDateFilter == label) Color(0xFF7A5EFF) else Color.Gray))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { navController.navigate("editProfile") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A5EFF)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Редактировать", color = Color.White)
            }
            OutlinedButton(
                onClick = { navController.navigate("settings") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Настройки")
            }
        }
    }
    selectedEvent?.let { event ->
        AlertDialog(
            onDismissRequest = viewModel::closeDialog,
            confirmButton = {
                TextButton(onClick = viewModel::closeDialog) {
                    Text("ОК")
                }
            },
            title = {
                Text(event.title + if (event.isFinished) " (Завершено)" else "", style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                Column {
                    Text(event.locationName, style = MaterialTheme.typography.bodyMedium)
                    Text(event.dateTime, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(event.description, style = MaterialTheme.typography.bodySmall)
                }
            },
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp
        )
    }
}