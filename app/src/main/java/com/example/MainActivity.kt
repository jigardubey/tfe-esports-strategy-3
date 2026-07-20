package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangePrimary
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Instantiate the local ViewModel
        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0D1117)),
                    bottomBar = {
                        // Hide bottom navigation in map_planner to allow maximum fullscreen drawing focus!
                        if (currentScreen != "map_planner") {
                            EliteBottomNavigation(
                                currentScreen = currentScreen,
                                onTabSelected = { tab -> viewModel.navigateTo(tab) }
                            )
                        }
                    },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            "home" -> HomeScreen(
                                viewModel = viewModel,
                                onNavigate = { screen -> viewModel.navigateTo(screen) }
                            )
                            "map_planner" -> MapPlannerScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateTo("home") }
                            )
                            "tournament" -> TournamentPlannerScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateTo("home") }
                            )
                            "notes" -> NotesScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateTo("home") }
                            )
                            "downloads" -> DownloadsScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateTo("home") }
                            )
                            "settings" -> SettingsScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateTo("home") }
                            )
                            else -> HomeScreen(
                                viewModel = viewModel,
                                onNavigate = { screen -> viewModel.navigateTo(screen) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EliteBottomNavigation(
    currentScreen: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .testTag("elite_bottom_navigation"),
        containerColor = Color(0xFF161B22),
        tonalElevation = 8.dp
    ) {
        val navItems = listOf(
            NavItem("home", "Home", Icons.Default.Home),
            NavItem("tournament", "Matches", Icons.Default.CalendarMonth),
            NavItem("notes", "Journal", Icons.Default.StickyNote2),
            NavItem("downloads", "Vault", Icons.Default.FolderSpecial)
        )

        navItems.forEach { item ->
            val isSelected = currentScreen == item.route || (item.route == "home" && currentScreen == "settings")
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Black else androidx.compose.ui.text.font.FontWeight.Bold
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = OrangeAccent,
                    selectedTextColor = OrangeAccent,
                    indicatorColor = OrangePrimary.copy(alpha = 0.15f),
                    unselectedIconColor = Color(0xFF8B949E),
                    unselectedTextColor = Color(0xFF8B949E)
                )
            )
        }
    }
}

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)
