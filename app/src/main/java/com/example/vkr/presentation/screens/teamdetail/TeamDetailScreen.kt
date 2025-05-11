package com.example.vkr.presentation.screens.teamdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.vkr.R
import com.example.vkr.data.AppDatabase
import com.example.vkr.data.model.EventEntity
import com.example.vkr.data.model.TeamEntity
import com.example.vkr.data.model.UserEntity
import com.example.vkr.data.session.UserSessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TeamDetailScreen(teamId: Int, navController: NavController) {
    val context = LocalContext.current
    val database = AppDatabase.getInstance(context)

    val viewModelState = remember { mutableStateOf(TeamDetailContract.ViewState()) }

    val view = remember {
        object : TeamDetailContract.View {
            override fun updateState(state: TeamDetailContract.ViewState) {
                viewModelState.value = state
            }

            override fun navigateToManageEvent(eventId: Int) {
                navController.navigate("manageEvent/$eventId")
            }

            override fun goBack() {
                navController.popBackStack()
            }
        }
    }

    val presenter = remember {
        TeamDetailPresenter(
            view = view,
            teamDao = database.teamDao(),
            userDao = database.userDao(),
            eventDao = database.eventDao(),
            session = UserSessionManager(context)
        )
    }

    LaunchedEffect(Unit) {
        presenter.onInit(teamId)
    }

    val state = viewModelState.value
    val team = state.team

    team?.let { t ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.team),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )
                Text(
                    text = t.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text("Очки: ${t.points}", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("Проведено мероприятий: ${state.events.size}", color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Участники команды", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (state.users.isEmpty()) {
                Text("Пока нет участников", style = MaterialTheme.typography.bodyMedium)
            } else {
                val pages = state.users.chunked(4)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(pages) { pageUsers ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.width(300.dp)
                        ) {
                            val rows = pageUsers.chunked(2)
                            rows.forEach { rowUsers ->
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    rowUsers.forEach { user ->
                                        Column(
                                            modifier = Modifier
                                                .width(140.dp)
                                                .height(80.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color(0xFFF0EFFF))
                                                .padding(12.dp),
                                            horizontalAlignment = Alignment.Start,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            if (user.avatarUri != null) {
                                                Image(
                                                    painter = rememberAsyncImagePainter(user.avatarUri),
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = Color(0xFF673AB7),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Text(user.name, color = Color(0xFF673AB7))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Мероприятия команды", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (state.events.isEmpty()) {
                Text("Пока нет мероприятий", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.events.sortedBy { it.isFinished }) { event ->
                        ElevatedCard(
                            modifier = Modifier
                                .width(220.dp)
                                .wrapContentHeight()
                                .clickable { presenter.onEventClicked(event.id) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = if (event.isFinished) Color(0xFFE0E0E0) else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                if (!event.imageUri.isNullOrEmpty()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(event.imageUri),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.LightGray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Нет фото", color = Color.DarkGray)
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                Text(event.title, fontWeight = FontWeight.Medium)
                                Text(formatDate(event.dateTime), color = Color.Gray)

                                if (event.isFinished) {
                                    Spacer(Modifier.height(4.dp))
                                    Text("Завершено", color = Color.Red, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            when {
                state.currentUser?.teamId == teamId -> {
                    OutlinedButton(onClick = { presenter.onLeaveTeam() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Покинуть команду")
                    }
                }
                state.currentUser?.teamId == null -> {
                    Button(onClick = { presenter.onJoinTeam() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Вступить в команду")
                    }
                }
                else -> {
                    Text(
                        "Вы уже состоите в другой команде.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(onClick = { presenter.onBackClicked() }, modifier = Modifier.fillMaxWidth()) {
                Text("Назад")
            }
        }
    }
}

fun formatDate(dateTime: String): String {
    return try {
        val parsed = LocalDateTime.parse(dateTime)
        parsed.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")))
    } catch (e: Exception) {
        ""
    }
}
