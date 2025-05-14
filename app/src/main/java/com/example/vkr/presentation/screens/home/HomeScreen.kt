package com.example.vkr.presentation.screens.home

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.vkr.R
import com.example.vkr.data.AppDatabase
import com.example.vkr.data.dao.EventDao
import com.example.vkr.data.dao.UserDao
import com.example.vkr.presentation.components.ActivityItem
import com.example.vkr.presentation.components.EventCard
import com.example.vkr.presentation.components.FilterCard

@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val eventDao = remember { AppDatabase.getInstance(context).eventDao() }
    val userDao = remember { AppDatabase.getInstance(context).userDao() }

    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(application, eventDao, userDao)
    )
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadEvents()
            }
        }
        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
    val state = viewModel.state
    val selectedEvent = viewModel.selectedEvent
    Column(modifier = modifier.padding(16.dp)) {
        Text("Добро пожаловать в", style = MaterialTheme.typography.bodyMedium)
        Text("CleanTogether", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchChanged,
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
                    viewModel.onFilterSelected(label)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        val favoriteEvents = state.filteredEvents.filter { it.isFavorite }
        Text("Избранные мероприятия", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        if (favoriteEvents.isEmpty()) {
            Text("Пока у вас нет избранных мероприятий.")
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(favoriteEvents) { event ->
                    val painter = if (!event.imageUri.isNullOrBlank()) {
                        rememberAsyncImagePainter(event.imageUri)
                    } else {
                        painterResource(id = R.drawable.testew)
                    }
                    EventCard(title = event.title, painter = painter) {
                        viewModel.onEventClick(event)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Ваши активности", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        if (state.filteredEvents.isEmpty()) {
            Text("Вы пока не присоединились ни к одному мероприятию.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.filteredEvents) { event ->
                    val painter = if (!event.imageUri.isNullOrBlank()) {
                        rememberAsyncImagePainter(event.imageUri)
                    } else {
                        painterResource(id = R.drawable.testew)
                    }
                    ActivityItem(
                        title = event.title,
                        subtitle = event.dateTime,
                        isFavorite = event.isFavorite,
                        painter = painter,
                        onFavoriteClick = { viewModel.onFavoriteToggle(event) },
                        onClick = { viewModel.onEventClick(event) }
                    )
                }
            }
        }
        selectedEvent?.let { event ->
            AlertDialog(
                onDismissRequest = viewModel::onDialogClose,
                confirmButton = {
                    TextButton(onClick = viewModel::onDialogClose) {
                        Text("Закрыть")
                    }
                },
                title = { Text(event.title, style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column {
                        Text("${event.locationName}")
                        Text("${event.dateTime}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(event.description)
                    }
                }
            )
        }
    }
}
class HomeViewModelFactory(
    private val application: Application,
    private val eventDao: EventDao,
    private val userDao: UserDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(eventDao, userDao, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}