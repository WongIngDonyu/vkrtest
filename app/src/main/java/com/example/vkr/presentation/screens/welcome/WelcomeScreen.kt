package com.example.vkr.presentation.screens.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vkr.R

@Composable
fun WelcomeScreen(navController: NavController, viewModel: WelcomeViewModel = viewModel()) {
    val navEvent by viewModel.navEvent.collectAsState()
    LaunchedEffect(navEvent) {
        navEvent?.let {
            navController.navigate(it)
            viewModel.onNavigationHandled()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.gg11),
                contentDescription = "Загрязнение",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text("CleanTogether", style = MaterialTheme.typography.headlineMedium.copy(color = Color.White))
                Text("Вместе за чистое будущее!", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White))
            }
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.ima1),
                contentDescription = "Уборка",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text("Субботники", style = MaterialTheme.typography.headlineMedium.copy(color = Color.White))
                Text("Присоединяйся к командам и внеси свой вклад!", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White))
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.onSignUpClicked() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Создать аккаунт")
            }
            OutlinedButton(
                onClick = { viewModel.onLoginClicked() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Войти")
            }
        }
    }
}