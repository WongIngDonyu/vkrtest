package com.example.vkr.presentation.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vkr.R
import com.example.vkr.data.AppDatabase
import com.example.vkr.presentation.components.RoleDropdown

@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    val userDao = remember { AppDatabase.getInstance(context).userDao() }

    var name by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var errors by remember { mutableStateOf(SignUpContract.ValidationErrors()) }

    val view = remember {
        object : SignUpContract.View {
            override fun showValidationErrors(e: SignUpContract.ValidationErrors) {
                errors = e
            }

            override fun navigateToLogin() {
                navController.navigate("login") {
                    popUpTo("signup") { inclusive = true }
                }
            }
        }
    }

    val presenter = remember { SignUpPresenter(view, userDao) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Создайте аккаунт", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Присоединяйтесь к движению за чистоту!", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // Поля
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Введите имя") },
            isError = errors.nameError,
            modifier = Modifier.fillMaxWidth()
        )
        if (errors.nameError) Text("Имя не может быть пустым", color = Color.Red)

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Введите никнейм") },
            isError = errors.nicknameError,
            modifier = Modifier.fillMaxWidth()
        )
        if (errors.nicknameError) Text("Никнейм должен быть не короче 3 символов", color = Color.Red)

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Введите номер телефона") },
            isError = errors.phoneError,
            modifier = Modifier.fillMaxWidth()
        )
        if (errors.phoneError) Text("Некорректный номер телефона", color = Color.Red)

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Введите пароль") },
            isError = errors.passwordError,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(Icons.Default.Visibility, contentDescription = "Показать пароль")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        if (errors.passwordError) Text("Пароль не может быть пустым", color = Color.Red)

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Повторите пароль") },
            isError = errors.confirmPasswordError,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(Icons.Default.Visibility, contentDescription = "Показать пароль")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        if (errors.confirmPasswordError) Text("Пароли не совпадают", color = Color.Red)

        Spacer(modifier = Modifier.height(12.dp))
        RoleDropdown(
            selectedRole = selectedRole,
            onRoleSelected = { selectedRole = it },
            isError = errors.roleError
        )
        if (errors.roleError) Text("Выберите роль", color = Color.Red)

        Spacer(modifier = Modifier.height(24.dp))

        // Кнопка
        Button(
            onClick = {
                presenter.onSignUpClicked(
                    name, nickname, phone, password, confirmPassword, selectedRole
                )
            },
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
