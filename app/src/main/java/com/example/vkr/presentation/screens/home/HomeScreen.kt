package com.example.vkr.presentation.screens.home

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.vkr.R
import com.example.vkr.data.AppDatabase
import com.example.vkr.data.model.EventEntity
import com.example.vkr.presentation.components.ActivityItem
import com.example.vkr.presentation.components.EventCard
import com.example.vkr.presentation.components.FilterCard
import com.example.vkr.ui.components.DateTimeUtils
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val eventDao = remember { AppDatabase.getInstance(context).eventDao() }
    val userDao = remember { AppDatabase.getInstance(context).userDao() }

    var selectedEvent by remember { mutableStateOf<EventEntity?>(null) }
    var state by remember { mutableStateOf(HomeContract.ViewState()) }

    val view = remember {
        object : HomeContract.View {
            override fun updateState(newState: HomeContract.ViewState) {
                state = newState
            }

            override fun showEventDetails(event: EventEntity) {
                selectedEvent = event
            }

            override fun hideEventDetails() {
                selectedEvent = null
            }
        }
    }

    val presenter = remember { HomePresenter(view, eventDao, userDao) }

    LaunchedEffect(Unit) {
        presenter.onInit()
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text("Добро пожаловать в", style = MaterialTheme.typography.bodyMedium)
        Text("CleanTogether", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.searchQuery, // просто строка
            onValueChange = { presenter.onSearchChanged(it) },
            placeholder = { Text("Поиск мероприятий") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("Все", "Предстоящие", "Популярные").forEach { label ->
                FilterCard(
                    icon = Icons.Default.Star,
                    label = label,
                    selected = state.selectedFilter == label
                ) {
                    presenter.onFilterSelected(label)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val favoriteEvents = state.allEvents.filter { it.isFavorite }

        Text("Избранные мероприятия", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (favoriteEvents.isEmpty()) {
            Text("Пока у вас нет избранных мероприятий.")
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(favoriteEvents) { event ->
                    val painter = if (!event.imageUri.isNullOrBlank()) {
                        rememberAsyncImagePainter(Uri.parse(event.imageUri))
                    } else {
                        painterResource(id = R.drawable.images)
                    }

                    EventCard(title = event.title, painter = painter) {
                        presenter.onEventClick(event)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Ваши активности", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (state.allEvents.isEmpty()) {
            Text("Нет мероприятий по выбранному фильтру.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.allEvents) { event ->
                    val painter = if (!event.imageUri.isNullOrBlank()) {
                        rememberAsyncImagePainter(Uri.parse(event.imageUri))
                    } else {
                        painterResource(id = R.drawable.images)
                    }

                    ActivityItem(
                        title = event.title,
                        subtitle = event.dateTime,
                        isFavorite = event.isFavorite,
                        painter = painter,
                        onFavoriteClick = { presenter.onFavoriteToggle(event) },
                        onClick = { presenter.onEventClick(event) }
                    )
                }
            }
        }

        selectedEvent?.let { event ->
            AlertDialog(
                onDismissRequest = { presenter.onDialogClose() },
                confirmButton = {
                    TextButton(onClick = { presenter.onDialogClose() }) {
                        Text("Закрыть")
                    }
                },
                title = { Text(event.title, style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column {
                        Text("📍 ${event.locationName}")
                        Text("🗓 ${event.dateTime}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(event.description)
                    }
                }
            )
        }
    }
}
