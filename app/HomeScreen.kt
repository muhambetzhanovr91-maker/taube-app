package kz.taube.app

import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import java.util.Locale

data class HomePrayerItem(
    val icon: String,
    val name: String,
    val time: String,
    val isActive: Boolean
)

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var cityName by remember { mutableStateOf("Анықталуда...") }

    LaunchedEffect(Unit) {
        getUserLocation(context) { loc ->
            if (loc != null) {
                cityName = getCityNameFromCoords(context, loc.latitude, loc.longitude)
            } else {
                cityName = "Атырау"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cityName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "📍", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Бүгін • Намаз кестесі",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            IconButton(onClick = { }) {
                Text(text = "🔔", fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D5C3A))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "КЕЛЕСІ НАМАЗ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFA7F3D0),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Бесін",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "13:05",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "02:34:56 қалды",
                        fontSize = 12.sp,
                        color = Color(0xFFD1FAE5)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Бүгінгі намаз уақыттары",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(12.dp))

        val prayerList = listOf(
            HomePrayerItem("🌅", "Таң (Субх)", "03:50", false),
            HomePrayerItem("☀️", "Күн шығуы", "05:28", false),
            HomePrayerItem("🕌", "Бесін", "13:05", true),
            HomePrayerItem("🌆", "Екінті", "17:17", false),
            HomePrayerItem("🌇", "Ақшам", "20:30", false),
            HomePrayerItem("🌙", "Құптан (Иша)", "22:05", false)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            prayerList.forEach { item ->
                PrayerRowCard(item)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun getUserLocation(context: Context, onLocationReceived: (Location?) -> Unit) {
    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            onLocationReceived(location)
        }.addOnFailureListener {
            onLocationReceived(null)
        }
    } catch (e: Exception) {
        onLocationReceived(null)
    }
}

private fun getCityNameFromCoords(context: Context, lat: Double, lon: Double): String {
    return try {
        val geocoder = Geocoder(context, Locale("kk"))
        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocation(lat, lon, 1)
        if (!addresses.isNullOrEmpty()) {
            addresses[0].locality ?: addresses[0].subAdminArea ?: "Атырау"
        } else {
            "Атырау"
        }
    } catch (e: Exception) {
        "Атырау"
    }
}

@Composable
private fun PrayerRowCard(data: HomePrayerItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (data.isActive) Color(0xFFE6F4ED) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (data.isActive) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = data.icon, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = data.name,
                    fontSize = 15.sp,
                    fontWeight = if (data.isActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (data.isActive) Color(0xFF0D5C3A) else Color(0xFF1E293B)
                )
            }
            Text(
                text = data.time,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (data.isActive) Color(0xFF0D5C3A) else Color(0xFF1E293B)
            )
        }
    }
}
