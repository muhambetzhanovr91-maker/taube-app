package kz.taube.app

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainAppStructure()
            }
        }
    }
}

@Composable
fun MainAppStructure() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("🕌", fontSize = 20.sp) },
                    label = { Text("Намаз", fontSize = 12.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("📖", fontSize = 20.sp) },
                    label = { Text("Құран мен Зікір", fontSize = 12.sp) }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomeScreen()
                1 -> QuranDuaScreen()
            }
        }
    }
}

// ---------------- НАМАЗ КЕСТЕСІ ЭКРАНЫ ----------------

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
            Column(
                modifier = Modifier.clickable {
                    Toast.makeText(context, "Орналасқан жер: $cityName", Toast.LENGTH_SHORT).show()
                }
            ) {
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

            IconButton(onClick = {
                Toast.makeText(context, "Хабарландырулар қосылды", Toast.LENGTH_SHORT).show()
            }) {
                Text(text = "🔔", fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clickable {
                    Toast.makeText(context, "Келесі намаз: Бесін (13:05)", Toast.LENGTH_SHORT).show()
                },
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
                PrayerRowCard(data = item, onClick = {
                    Toast.makeText(context, "${item.name} уақыты: ${item.time}", Toast.LENGTH_SHORT).show()
                })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ---------------- ҚҰРАН ЖӘНЕ ЗІКІРЛЕР ЭКРАНЫ ----------------

data class DuaModel(
    val title: String,
    val category: String,
    val arabicText: String,
    val translation: String
)

@Composable
fun QuranDuaScreen() {
    val context = LocalContext.current

    val duaList = listOf(
        DuaModel(
            title = "Фатиха сүресі",
            category = "Құран сүрелері",
            arabicText = "Бисмилләһир-рахмаанир-рахим. Әлхамду лилләһи раббил-ғаләмин...",
            translation = "Аса қамқор, ерекше мейірімді Алланың атымен бастаймын. Барлық мақтау-мадақ әлемдердің Раббысы Аллаға тән..."
        ),
        DuaModel(
            title = "Аятүл-Күрси",
            category = "Құран аяттары",
            arabicText = "Аллаһу лә иләһә иллә һуәл-Хаййул-Қаййум...",
            translation = "Алла — Одан басқа ешбір құдай жоқ. Ол Тірі, Әрі Әрқашан Бар Болушы..."
        ),
        DuaModel(
            title = "Таңғы зікір",
            category = "Күнделікті зікірлер",
            arabicText = "Субханаллаһи уә бихамдиһи",
            translation = "Алла Пәк әрі барлық мақтау Оған тән (Күніне 100 рет)"
        ),
        DuaModel(
            title = "Ықылас сүресі",
            category = "Құран сүрелері",
            arabicText = "Құл һуаллаһу ахад. Аллаһус-самад...",
            translation = "Айт: Ол Алла — Біреу. Алла — Самәд (ешкімге мұқтаж емес)..."
        ),
        DuaModel(
            title = "Іс бастардағы дуа",
            category = "Күнделікті дуалар",
            arabicText = "Рабби йассир уә лә туғассир, Рабби тәммим бил-хайр",
            translation = "Раббым! Жеңілдет, ауырлаптама. Раббым, ісімді қайырлы аяқтауды нәсіп ет."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Құран мен Зікірлер",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Text(
            text = "Күнделікті дуалар, зікірлер мен сүрелер",
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(duaList) { item ->
                DuaCard(item = item, onClick = {
                    Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                })
            }
        }
    }
}

@Composable
fun DuaCard(item: DuaModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = item.category.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D5C3A),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.arabicText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.translation,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

// ---------------- КӨМЕКШІ ФУНКЦИЯЛАР ----------------

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
private fun PrayerRowCard(data: HomePrayerItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
