package com.example.vkr.presentation.screens.createevent

import android.app.Application
import android.net.Uri
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.vkr.data.AppDatabase
import com.example.vkr.ui.components.TeamArea
import com.example.vkr.ui.components.getPolygonCenter
import com.example.vkr.ui.components.isPointInsidePolygon
import com.example.vkr.ui.components.parsePoints
import com.example.vkr.ui.components.rememberMapViewWithLifecycle
import com.yandex.mapkit.geometry.LinearRing
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polygon
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.PolygonMapObject
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.yandex.mapkit.map.Map

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(navController: NavController) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val viewModel: CreateEventViewModel = viewModel(
        factory = CreateEventViewModelFactory(context.applicationContext as Application)
    )
    var launcher by remember {
        mutableStateOf<ManagedActivityResultLauncher<String, Uri?>?>(null)
    }
    val mapView = rememberMapViewWithLifecycle()
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(initialHour = 12, initialMinute = 0, is24Hour = true)
    val state = viewModel.state
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()) }
    var teamAreas by remember { mutableStateOf<List<TeamArea>>(emptyList()) }
    val teamDao = remember { AppDatabase.getInstance(context).teamDao() }

    LaunchedEffect(Unit) {
        teamAreas = teamDao.getAllTeams().map {
            TeamArea(
                teamId = it.id,
                teamName = it.name,
                points = parsePoints(it.areaPoints),
                color = it.color
            )
        }
    }

    launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.onImagePicked(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Создание мероприятия", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.title,
            onValueChange = viewModel::onTitleChange,
            label = { Text("Название") },
            isError = state.titleError,
            modifier = Modifier.fillMaxWidth()
        )
        if (state.titleError)
            Text("Поле не может быть пустым", color = colorScheme.error)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text("Описание") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.location,
            onValueChange = viewModel::onLocationChange,
            label = { Text("Место проведения") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Text("Выберите место на карте:", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            if (teamAreas.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                AndroidView(
                    factory = {
                        mapView.map.move(CameraPosition(Point(55.529338, 37.514810), 16.0f, 0.0f, 0.0f))
                        mapView.map.apply {
                            isZoomGesturesEnabled = false
                            isScrollGesturesEnabled = true
                            isRotateGesturesEnabled = false
                            isTiltGesturesEnabled = false
                        }
                        val polygons = mutableMapOf<PolygonMapObject, TeamArea>()
                        val mapObjects = mapView.map.mapObjects
                        mapObjects.clear()
                        teamAreas.forEach { area ->
                            if (area.points.size >= 3) {
                                val polygon = Polygon(LinearRing(area.points), emptyList())
                                val obj = mapObjects.addPolygon(polygon)
                                obj.fillColor = area.color
                                obj.strokeColor = colorScheme.onSurface.toArgb()
                                obj.strokeWidth = 2f
                                polygons[obj] = area
                                val center = getPolygonCenter(area.points)
                                mapObjects.addPlacemark(center).setText(area.teamName)
                            }
                        }
                        mapView.map.addInputListener(object : InputListener {
                            override fun onMapTap(map: Map, point: Point) {
                                polygons.entries.firstOrNull {
                                    isPointInsidePolygon(it.value.points, point)
                                }?.let {
                                    viewModel.onTeamSelected(it.value.teamId)
                                }
                            }
                            override fun onMapLongTap(map: Map, point: Point) {}
                        })
                        mapView
                    },
                    modifier = Modifier.matchParentSize()
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = if (!state.selectedTeamName.isNullOrBlank())
                "Вы выбрали команду: ${state.selectedTeamName}"
            else
                "Нажмите на зону на карте, чтобы выбрать команду",
            style = MaterialTheme.typography.bodyMedium,
            color = if (state.selectedTeamName != null) colorScheme.primary else colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { viewModel.onPickDate() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(state.selectedDate?.format(dateFormatter) ?: "Выбрать дату")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { viewModel.onPickTime() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(state.selectedTime?.format(timeFormatter) ?: "Выбрать время")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { launcher?.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.imageUri != null) "Выбрано" else "Выбрать фото")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    viewModel.onCreateEvent {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                )
            ) {
                Text("Создать")
            }

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Отмена")
            }
        }
    }

    if (state.showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { viewModel.onDismissDate() },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onDateSelected(it) }
                }) {
                    Text("ОК")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDate() }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState, modifier = Modifier.fillMaxWidth())
        }
    }

    if (state.showTimePicker) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissTime() },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onTimeSelected(timePickerState.hour, timePickerState.minute)
                }) {
                    Text("OK", color = colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissTime() }) {
                    Text("Отмена", color = colorScheme.primary)
                }
            },
            title = { Text("Выберите время") },
            text = {
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colorScheme.surfaceVariant
        )
    }
}
