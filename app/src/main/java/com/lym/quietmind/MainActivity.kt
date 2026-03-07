package com.lym.quietmind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.navigation
import com.lym.quietmind.ui.dashboard.DashboardScreen
import com.lym.quietmind.ui.settings.SettingsScreen
import com.lym.quietmind.ui.settings.SyncBackupScreen
import com.lym.quietmind.ui.theme.QuietMindTheme
import com.lym.quietmind.ui.timer.TimerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuietMindTheme {
                QuietMindApp()
            }
        }
    }
}

@Composable
fun QuietMindApp() {
    val navController = rememberNavController()
    
    // Bottom Navigation Setup
    val items = listOf(
        Pair("timer", Icons.Filled.PlayArrow) to "专注",
        Pair("dashboard", Icons.Filled.List) to "洞察",
        Pair("settings_graph", Icons.Filled.Settings) to "设置"
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { (routeIconPair, label) ->
                    val route = routeIconPair.first
                    val icon = routeIconPair.second
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "timer",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("timer") {
                TimerScreen()
            }
            composable("dashboard") {
                DashboardScreen()
            }
            navigation(
                startDestination = "settings_home",
                route = "settings_graph"
            ) {
                composable("settings_home") {
                    SettingsScreen(
                        onNavigateToSync = { navController.navigate("settings_sync_backup") }
                    )
                }
                composable("settings_sync_backup") {
                    SyncBackupScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
