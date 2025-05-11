package com.example.vkr.presentation.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.vkr.data.session.UserSessionManager

@Composable
fun SplashScreen(navController: NavHostController) {
    val context = LocalContext.current
    val view = remember {
        object : SplashContract.View {
            override fun navigateToMain() {
                navController.navigate("main") {
                    popUpTo("auth") { inclusive = true }
                }
            }

            override fun navigateToAuth() {
                navController.navigate("auth") {
                    popUpTo(0)
                }
            }
        }
    }

    val presenter = remember { SplashPresenter(view, UserSessionManager(context)) }

    LaunchedEffect(Unit) {
        presenter.checkSession()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text("CleanTogether", style = MaterialTheme.typography.headlineLarge)
    }
}