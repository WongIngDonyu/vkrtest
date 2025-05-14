package com.example.vkr.presentation.screens.manageevent

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

@Composable
fun ManageEventScreen(eventId: String, navController: NavController) {
    val context = LocalContext.current
    val viewModel: ManageEventViewModel = viewModel()

    val event = viewModel.event
    val teamName = viewModel.teamName
    val photoUris = viewModel.photoUris

    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        viewModel.onImagePicked(uris)
    }

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
            Text(teamName, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            Spacer(Modifier.height(24.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!event.isFinished) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF2EBFF))
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", style = MaterialTheme.typography.headlineLarge, color = Color.Black)
                        }
                    }
                }
                items(photoUris) { uri ->
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
            Spacer(Modifier.height(32.dp))
            if (!event.isFinished) {
                Button(
                    onClick = {
                        viewModel.finishEvent {
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A5EFF))
                ) {
                    Text("Завершить мероприятие", color = Color.White)
                }
                Spacer(Modifier.height(12.dp))
            }
            OutlinedButton(
                onClick = { navController.popBackStack() },
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
