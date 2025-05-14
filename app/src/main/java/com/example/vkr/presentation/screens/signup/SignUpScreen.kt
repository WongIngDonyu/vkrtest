package com.example.vkr.presentation.signup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vkr.presentation.screens.signup.SignUpViewModel

@Composable
fun SignUpScreen(navController: NavController) {
    val viewModel = remember { SignUpViewModel() }

    LaunchedEffect(viewModel.navigateToLogin) {
        if (viewModel.navigateToLogin) {
            navController.navigate("login") {
                popUpTo("signup") { inclusive = true }
            }
            viewModel.onNavigationHandled()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Создайте аккаунт", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Присоединяйтесь к движению за чистоту!", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = viewModel.name,
            onValueChange = { viewModel.name = it },
            label = { Text("Введите имя") },
            isError = viewModel.nameError,
            modifier = Modifier.fillMaxWidth()
        )
        if (viewModel.nameError) Text("Имя не может быть пустым", color = Color.Red)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.nickname,
            onValueChange = { viewModel.nickname = it },
            label = { Text("Введите никнейм") },
            isError = viewModel.nicknameError,
            modifier = Modifier.fillMaxWidth()
        )
        if (viewModel.nicknameError) Text("Минимум 3 символа", color = Color.Red)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.phone,
            onValueChange = { viewModel.phone = it },
            label = { Text("Введите номер телефона") },
            isError = viewModel.phoneError,
            modifier = Modifier.fillMaxWidth()
        )
        if (viewModel.phoneError) Text("Некорректный номер", color = Color.Red)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = { Text("Введите пароль") },
            isError = viewModel.passwordError,
            visualTransformation = if (viewModel.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = viewModel::togglePasswordVisibility) {
                    Icon(Icons.Default.Visibility, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        if (viewModel.passwordError) Text("Пароль не может быть пустым", color = Color.Red)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.confirmPassword,
            onValueChange = { viewModel.confirmPassword = it },
            label = { Text("Повторите пароль") },
            isError = viewModel.confirmPasswordError,
            visualTransformation = if (viewModel.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = viewModel::togglePasswordVisibility) {
                    Icon(Icons.Default.Visibility, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        if (viewModel.confirmPasswordError) Text("Пароли не совпадают", color = Color.Red)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = viewModel::signUp,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A5EFF))
        ) {
            Text("Создать аккаунт", color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Уже есть аккаунт? Войти",
            modifier = Modifier
                .clickable { navController.navigate("login") }
                .padding(top = 8.dp),
            color = Color.Black
        )
    }
}