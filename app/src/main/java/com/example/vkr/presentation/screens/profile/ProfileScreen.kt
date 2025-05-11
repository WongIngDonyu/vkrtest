package com.example.vkr.presentation.screens.profile

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.vkr.R
import com.example.vkr.data.AppDatabase
import com.example.vkr.data.model.UserEntity
import com.example.vkr.data.session.UserSessionManager
import com.example.vkr.data.model.EventEntity
import com.example.vkr.presentation.components.AchievementCard
import com.example.vkr.presentation.components.EventCard2
import com.example.vkr.presentation.components.StatCard
import com.example.vkr.ui.components.DateTimeUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ProfileScreen(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val view = remember {
        object : ProfileContract.View {
            override fun navigateToEditProfile() {
                navController.navigate("editProfile")
            }
            override fun navigateToSettings() {
                navController.navigate("settings")
            }
        }
    }
    val presenter = remember { ProfilePresenter(view, context).apply { onInit() } }
    val state by presenter.state.collectAsState()

    val filteredEvents = state.events.filter {
        val eventDate = DateTimeUtils.parseDisplayFormatted(it.dateTime)
        eventDate?.let { date ->
            if (state.selectedDateFilter == "Предстоящие") date.isAfter(LocalDateTime.now())
            else date.isBefore(LocalDateTime.now())
        } ?: false
    }

    val avatarPainter = if (!state.user?.avatarUri.isNullOrBlank()) {
        rememberAsyncImagePainter(Uri.parse(state.user?.avatarUri))
    } else {
        painterResource(R.drawable.images)
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = avatarPainter,
                contentDescription = "Аватар",
                modifier = Modifier.size(72.dp).clip(CircleShape).border(2.dp, Color.Gray, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(state.user?.name ?: "Имя", style = MaterialTheme.typography.titleLarge)
                Text("@${state.user?.nickname ?: "ник"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(state.user?.role ?: "роль", style = MaterialTheme.typography.bodyMedium)
                Text("${state.team?.name?.let { "В команде: $it" } ?: "Без команды"}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Достижения", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.achievements) { achievement ->
                AchievementCard(title = achievement.title, subtitle = achievement.description, imageRes = achievement.imageResId)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Очков", state.user?.points?.toString() ?: "0", Icons.Default.Star)
            StatCard("Мероприятий", state.user?.eventCount?.toString() ?: "0", Icons.Default.Event)
        }

        Text("Мои мероприятия", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 12.dp))

        if (filteredEvents.isEmpty()) {
            Text("Нет мероприятий по выбранному фильтру.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredEvents) { event ->
                    val painter = if (!event.imageUri.isNullOrBlank()) rememberAsyncImagePainter(Uri.parse(event.imageUri)) else painterResource(id = R.drawable.images)
                    EventCard2(title = event.title, datePlace = event.dateTime, painter = painter) {
                        presenter.onEventSelected(event)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("Предстоящие", "Прошедшие").forEach { label ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { presenter.onDateFilterSelected(label) }) {
                    Icon(
                        imageVector = if (label == "Предстоящие") Icons.Default.CalendarToday else Icons.Default.History,
                        contentDescription = label,
                        tint = if (state.selectedDateFilter == label) Color(0xFF7A5EFF) else Color.Gray
                    )
                    Text(label, style = MaterialTheme.typography.labelMedium.copy(color = if (state.selectedDateFilter == label) Color(0xFF7A5EFF) else Color.Gray))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { presenter.onEditProfileClicked() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A5EFF)), shape = RoundedCornerShape(24.dp)) {
                Text("Редактировать", color = Color.White)
            }
            OutlinedButton(onClick = { presenter.onSettingsClicked() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp)) {
                Text("Настройки")
            }
        }
    }

    state.selectedEvent?.let { event ->
        AlertDialog(
            onDismissRequest = { presenter.onDialogDismissed() },
            confirmButton = {
                TextButton(onClick = { presenter.onDialogDismissed() }) {
                    Text("ОК")
                }
            },
            title = {
                Text(
                    text = event.title + if (event.isFinished) " (Завершено)" else "",
                    style = MaterialTheme.typography.headlineSmall
                )
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
