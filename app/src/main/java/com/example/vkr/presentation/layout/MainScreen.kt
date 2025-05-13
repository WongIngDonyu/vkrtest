package com.example.vkr.presentation.layout

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.vkr.ui.components.BottomNavigationBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.vkr.data.AppDatabase
import com.example.vkr.data.session.UserSessionManager
import com.example.vkr.presentation.screens.events.EventsScreen
import com.example.vkr.presentation.screens.home.HomeScreen
import com.example.vkr.presentation.screens.profile.ProfileScreen
import com.example.vkr.presentation.screens.search.SearchScreen

@Composable
fun MainScreen(currentRoute: String, navController: NavHostController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val session = remember { UserSessionManager(context) }
    val role by session.userRole.collectAsState(initial = null)

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                )
            }
        },
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
        },
        floatingActionButton = {
            if (currentRoute == "events" && role == "ORGANIZER") {
                FloatingActionButton(
                    onClick = { navController.navigate("create_event") },
                    containerColor = Color(0xFF7A5EFF),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Создать мероприятие",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { padding ->
        when (currentRoute) {
            "home" -> HomeScreen(navController, Modifier.padding(padding))
            "search" -> SearchScreen(navController)
            "events" -> EventsScreen(
                modifier = Modifier.padding(padding),
                snackbarHostState = snackbarHostState,
                navController = navController // если нужен
            )
            "profile" -> ProfileScreen(navController, Modifier.padding(padding))
        }
    }
}