package com.example.vkr.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vkr.data.session.UserSessionManager
import com.example.vkr.presentation.components.SettingToggle

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val session = remember { UserSessionManager(context) }
    var state by remember { mutableStateOf(SettingsContract.ViewState()) }

    val view = remember {
        object : SettingsContract.View {
            override fun updateState(newState: SettingsContract.ViewState) {
                state = newState
            }

            override fun navigateToWelcome() {
                navController.navigate("welcome") {
                    popUpTo("main") { inclusive = true }
                }
            }
        }
    }

    val presenter = remember { SettingsPresenter(view, session) }

    LaunchedEffect(Unit) {
        presenter.onInit()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Настройки уведомлений", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        SettingToggle("Push-уведомления", state.pushEnabled) {
            presenter.onToggle("push", it)
        }
        SettingToggle("Email-уведомления", state.emailEnabled) {
            presenter.onToggle("email", it)
        }
        SettingToggle("Напоминания о мероприятиях", state.remindersEnabled) {
            presenter.onToggle("reminders", it)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Прочее", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        SettingToggle("Геолокация", state.locationEnabled) {
            presenter.onToggle("location", it)
        }
        SettingToggle("Доступ к камере", state.cameraAccessEnabled) {
            presenter.onToggle("camera", it)
        }
        SettingToggle("Тёмная тема", state.darkModeEnabled) {
            presenter.onToggle("dark", it)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { presenter.onLogoutClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF3B30),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Выйти из аккаунта")
        }
    }

    if (state.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { presenter.onLogoutCancel() },
            title = { Text("Выход из аккаунта") },
            text = { Text("Вы уверены, что хотите выйти?") },
            confirmButton = {
                TextButton(onClick = { presenter.onLogoutConfirm() }) {
                    Text("Да")
                }
            },
            dismissButton = {
                TextButton(onClick = { presenter.onLogoutCancel() }) {
                    Text("Отмена")
                }
            }
        )
    }
}
