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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.vkr.data.session.UserSessionManager
import com.example.vkr.presentation.components.SettingToggle

@Composable
fun SettingsScreen(navController: NavController) {
    val viewModel: SettingsViewModel = viewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Настройки уведомлений", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        SettingToggle("Push-уведомления", viewModel.pushEnabled) {
            viewModel.onToggle("push", it)
        }
        SettingToggle("Email-уведомления", viewModel.emailEnabled) {
            viewModel.onToggle("email", it)
        }
        SettingToggle("Напоминания о мероприятиях", viewModel.remindersEnabled) {
            viewModel.onToggle("reminders", it)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Прочее", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        SettingToggle("Геолокация", viewModel.locationEnabled) {
            viewModel.onToggle("location", it)
        }
        SettingToggle("Доступ к камере", viewModel.cameraAccessEnabled) {
            viewModel.onToggle("camera", it)
        }
        SettingToggle("Тёмная тема", viewModel.darkModeEnabled) {
            viewModel.onToggle("dark", it)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = viewModel::onLogoutClick,
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

    if (viewModel.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onLogoutCancel,
            title = { Text("Выход из аккаунта") },
            text = { Text("Вы уверены, что хотите выйти?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onLogoutConfirm {
                        navController.navigate("welcome") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                }) {
                    Text("Да")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onLogoutCancel) {
                    Text("Отмена")
                }
            }
        )
    }
}

