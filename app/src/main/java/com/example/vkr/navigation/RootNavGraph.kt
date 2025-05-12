package com.example.vkr.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.vkr.presentation.layout.MainScreen
import com.example.vkr.presentation.screens.createevent.CreateEventScreen
import com.example.vkr.presentation.screens.editprofile.EditProfileScreen
import com.example.vkr.presentation.screens.login.LoginScreen
import com.example.vkr.presentation.screens.manageevent.ManageEventScreen
import com.example.vkr.presentation.screens.settings.SettingsScreen
import com.example.vkr.presentation.screens.splash.SplashScreen
import com.example.vkr.presentation.screens.teamdetail.TeamDetailScreen
import com.example.vkr.presentation.screens.welcome.WelcomeScreen
import com.example.vkr.presentation.signup.SignUpScreen


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RootNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }

        navigation(startDestination = "welcome", route = "auth") {
            composable("welcome") { WelcomeScreen(navController) }
            composable("login") { LoginScreen(navController) }
            composable("signup") { SignUpScreen(navController) }
        }

        navigation(startDestination = "home", route = "main") {
            composable("home") { MainScreen("home", navController) }
            composable("search") { MainScreen("search", navController) }
            composable("events") { MainScreen("events", navController) }
            composable("profile") { MainScreen("profile", navController) }
            composable("settings") { SettingsScreen(navController) }
            composable("editProfile") { EditProfileScreen(navController) }
            composable("create_event") { CreateEventScreen(navController) }
            composable("teamDetail/{teamId}") { backStackEntry ->
                val teamId = backStackEntry.arguments?.getString("teamId")
                teamId?.let {
                    TeamDetailScreen(teamId = it, navController = navController)
                }
            }
            composable("manageEvent/{eventId}") { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId")?.toIntOrNull()
                if (eventId != null) {
                    ManageEventScreen(eventId, navController)
                }
            }
        }
    }
}