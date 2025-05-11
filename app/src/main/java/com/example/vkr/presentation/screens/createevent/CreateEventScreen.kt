package com.example.vkr.presentation.screens.createevent

import android.net.Uri
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.yandex.mapkit.map.Map

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(navController: NavController) {
    val context = LocalContext.current
    val navControllerRemembered = rememberUpdatedState(navController)
    val teamDao = remember { AppDatabase.getInstance(context).teamDao() }

    var launcher by remember {
        mutableStateOf<ManagedActivityResultLauncher<String, Uri?>?>(null)
    }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(
        initialHour = 12,
        initialMinute = 0,
        is24Hour = true
    )

    val view = remember {
        object : CreateEventContract.View {
            override fun openImagePicker() {
                launcher?.launch("image/*")
            }

            override fun goBack() {
                navControllerRemembered.value.popBackStack()
            }
        }
    }

    val presenter = remember { CreateEventPresenter(context, navController, view) }
    val state by presenter.state.collectAsState()

    launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        presenter.onImagePicked(uri)
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()) }

    var teamAreas by remember { mutableStateOf<List<TeamArea>>(emptyList()) }
    val mapView = rememberMapViewWithLifecycle()

    LaunchedEffect(Unit) {
        teamAreas = teamDao.getAllTeams().map { team ->
            TeamArea(
                teamId = team.id,
                teamName = team.name,
                points = parsePoints(team.areaPoints),
                color = team.color
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Создание мероприятия", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.title,
            onValueChange = { presenter.onTitleChange(it) },
            label = { Text("Название") },
            isError = state.titleError,
            modifier = Modifier.fillMaxWidth()
        )
        if (state.titleError) Text("Поле не может быть пустым", color = Color.Red)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.description,
            onValueChange = { presenter.onDescriptionChange(it) },
            label = { Text("Описание") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.location,
            onValueChange = { presenter.onLocationChange(it) },
            label = { Text("Место проведения") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Выберите место на карте:", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))

        val selectedTeamName = state.selectedTeamName

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp) // ⬅️ МЕНЬШАЯ КАРТА
                .clip(RoundedCornerShape(12.dp))
        ) {
            if (teamAreas.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                AndroidView(
                    factory = {
                        mapView.map.move(
                            CameraPosition(Point(55.529338, 37.514810), 16.0f, 0.0f, 0.0f)
                        )

                        mapView.map.isZoomGesturesEnabled = false
                        mapView.map.isScrollGesturesEnabled = true
                        mapView.map.isRotateGesturesEnabled = false
                        mapView.map.isTiltGesturesEnabled = false

                        val polygons = mutableMapOf<PolygonMapObject, TeamArea>()
                        val mapObjects = mapView.map.mapObjects
                        mapObjects.clear()

                        teamAreas.forEach { area ->
                            if (area.points.size >= 3) {
                                val polygon = Polygon(LinearRing(area.points), emptyList())
                                val obj = mapObjects.addPolygon(polygon)
                                obj.fillColor = area.color
                                obj.strokeColor = Color.Black.toArgb()
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
                                    presenter.onTeamSelected(it.value.teamId)
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

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (!selectedTeamName.isNullOrBlank())
                "Вы выбрали команду: $selectedTeamName"
            else
                "Нажмите на зону на карте, чтобы выбрать команду",
            style = MaterialTheme.typography.bodyMedium,
            color = if (selectedTeamName != null) Color(0xFF7A5EFF) else Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { presenter.onPickDate() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(state.selectedDate?.format(dateFormatter) ?: "Выбрать дату")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { presenter.onPickTime() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(state.selectedTime?.format(timeFormatter) ?: "Выбрать время")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { presenter.onPickImage() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.imageUri != null) "Выбрано" else "Выбрать фото")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { presenter.onCreateEvent() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A5EFF))
            ) {
                Text("Создать", color = Color.White)
            }

            OutlinedButton(
                onClick = { presenter.onCancel() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Отмена")
            }
        }
    }

    if (state.showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { presenter.onDismissDate() },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        presenter.onDateSelected(millis)
                    }
                }) {
                    Text("ОК")
                }
            },
            dismissButton = {
                TextButton(onClick = { presenter.onDismissDate() }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (state.showTimePicker) {
        AlertDialog(
            onDismissRequest = { presenter.onDismissTime() },
            confirmButton = {
                TextButton(onClick = {
                    presenter.onTimeSelected(
                        timePickerState.hour,
                        timePickerState.minute
                    )
                }) {
                    Text("OK", color = Color(0xFF7A5EFF))
                }
            },
            dismissButton = {
                TextButton(onClick = { presenter.onDismissTime() }) {
                    Text("Отмена", color = Color(0xFF7A5EFF))
                }
            },
            title = { Text("Выберите время") },
            text = {
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TimePickerDefaults.colors()
                )
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color(0xFFF9F6FF)
        )
    }
}
