package kz.taube.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaubeTheme {
                MainAppStructure()
            }
        }
    }
}

// Түстер палитрасы (Суреттегідей)
val PrimaryGreen = Color(0xFF0D5C3A)
val BackgroundLight = Color(0xFFF8FAFC)
val CardBackground = Color(0xFFFFFFFF)
val TextDark = Color(0xFF1E293B)
val TextMuted = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppStructure() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("🕌", fontSize = 18.sp) },
                    label = { Text("Басты", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("🧭", fontSize = 18.sp) },
                    label = { Text("Құбыла", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Text("📖", fontSize = 18.sp) },
                    label = { Text("Құран", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Text("📿", fontSize = 18.sp) },
                    label = { Text("Зікір", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Text("⚙️", fontSize = 18.sp) },
                    label = { Text("Қосымша", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundLight)
        ) {
            when (selectedTab) {
                0 -> HomeScreen()
                1 -> QiblaScreen()
                2 -> QuranScreen()
                3 -> DhikrScreen()
                4 -> MoreScreen()
            }
        }
    }
}

@Composable
fun TaubeTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
