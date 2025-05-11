package com.example.vkr.presentation.screens.manageevent

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
fun ManageEventScreen(eventId: Int, navController: NavController) {
    val context = LocalContext.current
    val database = AppDatabase.getInstance(context)

    var state by remember { mutableStateOf(ManageEventContract.ViewState()) }

    // 1. Создаём presenter сначала
    lateinit var presenter: ManageEventPresenter

    // 2. Создаём imagePickerLauncher, но после presenter будет привязан
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        presenter.onImagePicked(uris)
    }

    val view = remember {
        object : ManageEventContract.View {
            override fun updateState(newState: ManageEventContract.ViewState) {
                state = newState
            }

            override fun goBack() {
                navController.popBackStack()
            }

            override fun openImagePicker() {
                imagePickerLauncher.launch("image/*")
            }
        }
    }

    // 3. Теперь можно безопасно инициализировать
    presenter = remember {
        ManageEventPresenter(
            view = view,
            eventDao = database.eventDao(),
            teamDao = database.teamDao()
        )
    }

    LaunchedEffect(Unit) {
        presenter.onInit(eventId)
    }

    val event = state.event

    if (event != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (!event.imageUri.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(event.imageUri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(event.title, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(event.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))

            Text("Место", style = MaterialTheme.typography.labelMedium)
            Text(event.locationName, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            Spacer(Modifier.height(8.dp))

            Text("Команда", style = MaterialTheme.typography.labelMedium)
            Text(state.teamName, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)

            Spacer(Modifier.height(24.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!event.isFinished) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF2EBFF)) // лавандовый фон
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", style = MaterialTheme.typography.headlineLarge, color = Color.Black)
                        }
                    }
                }

                items(state.photoUris) { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!event.isFinished) {
                    item {
                        ElevatedCard(
                            modifier = Modifier.size(120.dp),
                            onClick = {
                                imagePickerLauncher.launch("image/*")
                            }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+", style = MaterialTheme.typography.headlineLarge)
                            }
                        }
                    }
                }

                items(state.photoUris) { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            if (!event.isFinished) {
                Button(
                    onClick = { presenter.onFinishEvent() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A5EFF))
                ) {
                    Text("Завершить мероприятие", color = Color.White)
                }

                Spacer(Modifier.height(12.dp))
            }

            OutlinedButton(
                onClick = { presenter.onBackClicked() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Назад")
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Мероприятие не найдено", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

