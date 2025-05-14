package com.example.vkr

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.rememberNavController
import com.example.vkr.navigation.RootNavGraph

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
           //deleteDatabase("vkr_database")
//        val session = UserSessionManager(applicationContext)
//        lifecycleScope.launch {
//            session.clearSession()
//        }
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                RootNavGraph(navController = navController)
            }
        }
    }
}