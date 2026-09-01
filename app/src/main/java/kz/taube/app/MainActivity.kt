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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import java.util.Locale

// ---------------- МОДЕЛЬДЕРДІ АНЫҚТАУ ----------------

data class HomePrayerItem(
    val icon: String,
    val name: String,
    val time: String,
    val isActive: Boolean
)

data class PrayerTimeData(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)

data class SurahModel(
    val id: Int,
    val name: String,
    val arabicTitle: String,
    val arabicText: String,
    val translation: String
)

data class DhikrDuaModel(
    val title: String,
    val category: String,
    val arabicText: String,
    val readKazakh: String,
    val translation: String
)

// ---------------- MAIN ACTIVITY ----------------

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

// ---------------- НЕГІЗГІ ҚҰРЫЛЫМ (BOTTOM NAVIGATION) ----------------

@Composable
fun MainAppStructure() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var currentCity by remember { mutableStateOf("Атырау") }

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
                    label = { Text("Намаз", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("📖", fontSize = 20.sp) },
                    label = { Text("Құран", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Text("📿", fontSize = 20.sp) },
                    label = { Text("Дұға мен Зікір", fontSize = 11.sp) }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomeScreen(currentCity = currentCity, onCityChange = { newCity -> currentCity = newCity })
                1 -> QuranScreen()
                2 -> DhikrDuaScreen()
            }
        }
    }
}

// ---------------- 1. НАМАЗ КЕСТЕСІ ЭКРАНЫ ----------------

@Composable
fun HomeScreen(currentCity: String, onCityChange: (String) -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showCityDialog by remember { mutableStateOf(false) }

    val cityTimes = mapOf(
        "Атырау" to PrayerTimeData("03:50", "05:28", "13:05", "17:17", "20:30", "22:05"),
        "Алматы" to PrayerTimeData("04:10", "05:45", "13:00", "16:50", "20:10", "21:40"),
        "Астана" to PrayerTimeData("03:40", "05:20", "13:10", "17:10", "20:50", "22:30"),
        "Шымкент" to PrayerTimeData("04:20", "05:50", "13:08", "16:55", "20:15", "21:45"),
        "Ақтөбе" to PrayerTimeData("03:55", "05:35", "13:15", "17:20", "20:45", "22:20")
    )

    val activeTimes = cityTimes[currentCity] ?: cityTimes["Атырау"]!!

    LaunchedEffect(Unit) {
        getUserLocation(context) { loc ->
            if (loc != null) {
                val detected = getCityNameFromCoords(context, loc.latitude, loc.longitude)
                if (cityTimes.containsKey(detected)) {
                    onCityChange(detected)
                }
            }
        }
    }

    if (showCityDialog) {
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = { Text("Қаланы таңдаңыз", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    cityTimes.keys.forEach { city ->
                        Text(
                            text = city,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCityChange(city)
                                    showCityDialog = false
                                }
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            },
            confirmButton = {}
        )
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
                modifier = Modifier.clickable { showCityDialog = true }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currentCity,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "📍 (ауыстыру)", fontSize = 12.sp, color = Color(0xFF0D5C3A))
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Бүгін • Намаз кестесі",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            IconButton(onClick = {
                Toast.makeText(context, "Хабарландырулар белсенді", Toast.LENGTH_SHORT).show()
            }) {
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
                        text = activeTimes.dhuhr,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
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
            HomePrayerItem("🌅", "Таң (Субх)", activeTimes.fajr, false),
            HomePrayerItem("☀️", "Күн шығуы", activeTimes.sunrise, false),
            HomePrayerItem("🕌", "Бесін", activeTimes.dhuhr, true),
            HomePrayerItem("🌆", "Екінті", activeTimes.asr, false),
            HomePrayerItem(""🌅", "Таң (Субх)", activeTimes.fajr, false),
            HomePrayerItem("☀️", "Күн шығуы", activeTimes.sunrise, false),
            HomePrayerItem("🕌", "Бесін", activeTimes.dhuhr, true),
            HomePrayerItem("🌆", "Екінті", activeTimes.asr, false),
            HomePrayerItem("🌇", "Ақшам", activeTimes.maghrib, false),
            HomePrayerItem("🌙", "Құптан (Иша)", activeTimes.isha, false)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            prayerList.forEach { item ->
                PrayerRowCard(data = item)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ---------------- 2. ҚҰРАН ЭКРАНЫ ----------------

@Composable
fun QuranScreen() {
    var showTranslation by remember { mutableStateOf(true) }

    val surahList = listOf(
        SurahModel(
            1, "Фатиха сүресі", "الفاتحة",
            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nالْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ\nالرَّحْمَٰنِ الرَّحِيمِ\nمَالِكِ يَوْمِ الدِّينِ",
            "Аса қамқор, ерекше мейірімді Алланың атымен бастаймын. Барлық мақтау-мадақ әлемдердің Раббысы Аллаға тән. Ол — Аса қамқор, ерекше мейірімді. Қиямет күнінің Иесі."
        ),
        SurahModel(
            112, "Ықылас сүресі", "الإخلاص",
            "قُلْ هُوَ اللَّهُ أَحَدٌ\nاللَّهُ الصَّمَدُ\nلَمْ يَلِدْ وَلَمْ يُولَدْ\nوَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
            "Айт: Ол Алла — Біреу. Алла — Самәд (ешкімге мұқтаж емес). Ол тумады да, туылмады. Әрі Оған ешкім тең келе алмайды."
        ),
        SurahModel(
            113, "Фалақ сүресі", "الفلق",
            "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ\nمِن شَرِّ مَا خَلَقَ",
            "Айт: Таңның Раббысына сиынамын, Оның жаратқан нәрселерінің жамандығынан..."
        ),
        SurahModel(
            114, "Нас сүресі", "الناس",
            "قُلْ أَعُوذُ بِرَبِّ النَّاسِ\nمَلِكِ النَّاسِ\nإِلَٰهِ النَّاسِ",
            "Айт: Адамдардың Раббысына, Адамдардың Патшасына, Адамдардың Тәңіріне сиынамын..."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Құран Кәрім", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Text(text = "Сүрелер тізімі", fontSize = 13.sp, color = Color(0xFF64748B))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Аударма", fontSize = 12.sp, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.width(4.dp))
                Switch(
                    checked = showTranslation,
                    onCheckedChange = { showTranslation = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(surahList) { surah ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${surah.id}. ${surah.name}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D5C3A)
                            )
                            Text(
                                text = surah.arabicTitle,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D5C3A)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = surah.arabicText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A),
                            lineHeight = 32.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (showTranslation) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = surah.translation,
                                fontSize = 13.sp,
                                color = Color(0xFF475569),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- 3. ДҰҒА МЕН ЗІКІР ЭКРАНЫ ----------------

@Composable
fun DhikrDuaScreen() {
    val itemsList = listOf(
        DhikrDuaModel(
            title = "Аятүл-Күрси",
            category = "Қасиетті Аят",
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
            readKazakh = "Аллаһу лә иләһә иллә һуәл-Хаййул-Қаййум...",
            translation = "Алла — Одан басқа ешбір құдай жоқ. Ол Тірі, Әрі Әрқашан Бар Болушы..."
        ),
        DhikrDuaModel(
            title = "Таңғы зікір",
            category = "Күнделікті зікір",
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            readKazakh = "Субханаллаһи уә бихамдиһи",
            translation = "Алла Пәк әрі барлық мақтау Оған тән (Күніне 100 рет)"
        ),
        DhikrDuaModel(
            title = "Іс бастардағы дұға",
            category = "Күнделікті дұға",
            arabicText = "رَبِّ يَسِّرْ وَلَا تُعَسِّرْ رَبِّ تَمِّمْ بِالْخَيْرِ",
            readKazakh = "Рабби йассир уә лә туғассир, Рабби тәммим бил-хайр",
            translation = "Раббым! Жеңілдет, ауырлатпа. Раббым, ісімді қайырлы аяқтауды нәсіп ет."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Дұғалар мен Зікірлер", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        Text(text = "Күнделікті оқылатын дұға-зікірлер", fontSize = 13.sp, color = Color(0xFF64748B))

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(itemsList) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = item.category.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D5C3A),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = item.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.arabicText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = item.readKazakh, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0D5C3A))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = item.translation, fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
            }
        }
    }
}

// ---------------- КӨМЕКШІ ФУНКЦИЯЛАР ЖӘНЕ КОМПОНЕНТТЕР ----------------

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
