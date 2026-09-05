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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import java.util.Locale

// =========================================================
// ТҮС ПАЛИТРАСЫ (мокаптағыдай: терең жасыл + алтын + крем)
// =========================================================

object TaubeColors {
    val DarkGreen = Color(0xFF0B3D2E)
    val DeepGreen = Color(0xFF0D5C3A)
    val Gold = Color(0xFFCBA135)
    val Cream = Color(0xFFF8F6F1)
    val CardWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextGray = Color(0xFF64748B)
    val ActiveBg = Color(0xFFE6F4ED)
}

// =========================================================
// МОДЕЛЬДЕР
// =========================================================

data class HomePrayerItem(
    val icon: ImageVector,
    val iconColor: Color,
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
    val translation: String,
    val versesInfo: String
)

data class DhikrItem(
    val title: String,
    val subtitle: String,
    val target: Int
)

data class MoreMenuItem(
    val icon: String,
    val title: String
)

// =========================================================
// MAIN ACTIVITY
// =========================================================

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

// =========================================================
// НЕГІЗГІ ҚҰРЫЛЫМ — 5 ТАБТЫ BOTTOM NAVIGATION
// =========================================================

private data class TabItem(val icon: ImageVector, val label: String)

private val tabs = listOf(
    TabItem(Icons.Filled.Home, "Басты бет"),
    TabItem(Icons.Filled.Explore, "Құбыла"),
    TabItem(Icons.Filled.MenuBook, "Құран"),
    TabItem(Icons.Filled.Favorite, "Зікір"),
    TabItem(Icons.Filled.Settings, "Қосымша")
)

@Composable
fun MainAppStructure() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var currentCity by remember { mutableStateOf("Атырау") }

    Scaffold(
        containerColor = TaubeColors.Cream,
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TaubeColors.DeepGreen,
                            selectedTextColor = TaubeColors.DeepGreen,
                            indicatorColor = TaubeColors.ActiveBg
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomeScreen(currentCity = currentCity, onCityChange = { newCity -> currentCity = newCity })
                1 -> QiblaScreen()
                2 -> QuranScreen()
                3 -> ZikrScreen()
                4 -> MoreScreen()
            }
        }
    }
}

// =========================================================
// 1. БАСТЫ БЕТ — Ассаламу ғалейкум! + келесі намаз + кесте
// =========================================================

@Composable
fun HomeScreen(currentCity: String, onCityChange: (String) -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showCityDialog by remember { mutableStateOf(false) }

    val cityTimes = mapOf(
        "Атырау" to PrayerTimeData("05:24", "06:51", "13:36", "18:13", "20:14", "21:41"),
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
                if (cityTimes.containsKey(detected)) onCityChange(detected)
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
            .background(TaubeColors.Cream)
            .verticalScroll(scrollState)
    ) {
        // ---------- Жасыл геройлық блок (грит + бармен) ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(TaubeColors.DarkGreen, TaubeColors.DeepGreen)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.clickable { showCityDialog = true }) {
                    Text(
                        text = "Ассаламу ғалейкум!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "19 Мамыр, жексенбі", fontSize = 13.sp, color = Color(0xFFCBD5C0))
                    Text(text = "10 Зулқада 1445", fontSize = 13.sp, color = Color(0xFFCBD5C0))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "📍 $currentCity  (ауыстыру)", fontSize = 12.sp, color = TaubeColors.Gold)
                }
                IconButton(onClick = {
                    Toast.makeText(context, "Хабарландырулар белсенді", Toast.LENGTH_SHORT).show()
                }) {
                    Text(text = "🔔", fontSize = 20.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---------- Келесі намаз картасы (шам-фонарь стилінде) ----------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF124A34))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "КЕЛЕСІ НАМАЗ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TaubeColors.Gold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Екінті",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = activeTimes.asr,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⏳ 04:01:01 қалды",
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5C0)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🔔  ", fontSize = 13.sp)
                        Text(text = "Аазан еске салғышы", fontSize = 13.sp, color = Color(0xFFCBD5C0))
                    }
                }
            }
        }

        // ---------- Бүгінгі намаз уақыттары ----------
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            Text(
                text = "БҮГІНГІ НАМАЗ УАҚЫТТАРЫ",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TaubeColors.TextGray,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val prayerList = listOf(
                HomePrayerItem(Icons.Filled.WbTwilight, Color(0xFFF59E0B), "Таң", activeTimes.fajr, false),
                HomePrayerItem(Icons.Filled.WbSunny, Color(0xFFFACC15), "Күн шығуы", activeTimes.sunrise, false),
                HomePrayerItem(Icons.Filled.AccountBalance, TaubeColors.DeepGreen, "Бесін", activeTimes.dhuhr, true),
                HomePrayerItem(Icons.Filled.Brightness5, Color(0xFF0EA5A0), "Екінті", activeTimes.asr, false),
                HomePrayerItem(Icons.Filled.WbTwilight, Color(0xFFEA580C), "Ақшам", activeTimes.maghrib, false),
                HomePrayerItem(Icons.Filled.NightsStay, Color(0xFF4C1D95), "Құптан", activeTimes.isha, false)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    prayerList.forEachIndexed { index, item ->
                        PrayerRowCard(data = item)
                        if (index != prayerList.lastIndex) {
                            Divider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TaubeColors.ActiveBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "«Еске алуармен жүректер тыныштық табады»",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TaubeColors.TextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Құран 13:28", fontSize = 11.sp, color = TaubeColors.TextGray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// =========================================================
// 2. ҚҰБЫЛА БАҒЫТЫ ЭКРАНЫ
// =========================================================

@Composable
fun QiblaScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(TaubeColors.DarkGreen, TaubeColors.DeepGreen))
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            Text(text = "Құбыла бағыты", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Icon(Icons.Filled.Settings, contentDescription = null, tint = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Құбыла бағыты", fontSize = 14.sp, color = Color(0xFFCBD5C0))
            Text(
                text = "248°",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(text = "Меккеге дейін 4321 км", fontSize = 13.sp, color = Color(0xFFCBD5C0))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ---------- Компас шеңбері ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(Color(0xFF0E4632)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.86f)
                    .clip(CircleShape)
                    .background(Color.Transparent)
            ) {
                Text("N", color = Color.White, fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp))
                Text("S", color = Color.White, fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp))
                Text("W", color = Color.White, fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp))
                Text("E", color = Color.White, fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp))
            }
            // Кааба белгісі
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF111827)),
                contentAlignment = Alignment.Center
            ) {
                Text("🕋", fontSize = 30.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF124A34))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Дәлдік", fontSize = 12.sp, color = Color(0xFFCBD5C0))
                    Text("±3°", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Card(
                modifier = Modifier.weight(1f).clickable { },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF124A34))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Калибрлеу", fontSize = 13.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// =========================================================
// 3. ҚҰРАН ЭКРАНЫ
// =========================================================

@Composable
fun QuranScreen() {
    var selectedFilter by remember { mutableIntStateOf(0) }
    val filters = listOf("Сүрелер", "Парақтар", "Сақталған")

    val surahList = listOf(
        SurahModel(1, "Әл-Фатиха сүресі", "الفاتحة",
            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            "Аса қамқор, ерекше мейірімді Алланың атымен бастаймын.", "Мекеде түсті • 7 аят"),
        SurahModel(2, "Әл-Бақара сүресі", "البقرة",
            "الم ذَٰلِكَ الْكِتَابُ لَا رَيْبَ فِيهِ",
            "Алиф, Лям, Мим. Бұл — Кітап, онда күмән жоқ.", "Мединеде түсті • 286 аят"),
        SurahModel(3, "Әли Имран сүресі", "آل عمران",
            "الم اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ",
            "Алиф, Лям, Мим. Алла — Одан басқа тәңір жоқ.", "Мединеде түсті • 200 аят"),
        SurahModel(4, "Ән-Ниса сүресі", "النساء",
            "يَا أَيُّهَا النَّاسُ اتَّقُوا رَبَّكُمُ",
            "Ей адамдар! Раббыларыңнан қорқыңдар.", "Мединеде түсті • 176 аят")
    )

    Column(modifier = Modifier.fillMaxSize().background(TaubeColors.Cream)) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(text = "Құран", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TaubeColors.TextDark)

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filters.forEachIndexed { index, filter ->
                    val active = selectedFilter == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (active) TaubeColors.DeepGreen else Color(0xFFEFEFEF))
                            .clickable { selectedFilter = index }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 13.sp,
                            color = if (active) Color.White else TaubeColors.TextGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TaubeColors.ActiveBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Соңғы оқылған", fontSize = 11.sp, color = TaubeColors.TextGray)
                    Text("Әл-Бақара сүресі", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TaubeColors.TextDark)
                    Text("Аят 255", fontSize = 12.sp, color = TaubeColors.TextGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = TaubeColors.DeepGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Жалғастыру")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Сүрелер тізімі", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TaubeColors.TextDark)
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(surahList) { surah ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${surah.id}. ${surah.name}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TaubeColors.TextDark
                        )
                        Text(text = surah.versesInfo, fontSize = 11.sp, color = TaubeColors.TextGray)
                    }
                    Text(text = surah.arabicTitle, fontSize = 16.sp, color = TaubeColors.DeepGreen)
                }
                Divider(color = Color(0xFFF1F5F9))
            }
        }
    }
}

// =========================================================
// 4. ЗІКІР ЭКРАНЫ
// =========================================================

@Composable
fun ZikrScreen() {
    var selectedCategory by remember { mutableIntStateOf(0) }
    val categories = listOf("Таңғы", "Кешкі", "Тасбих", "Дұғалар", "99 есім")

    val dhikrItems = listOf(
        DhikrItem("Аят әл-Күрси", "Бақара сүресі, 255-аят", 1),
        DhikrItem("3 Құл (Ихлас, Фалақ, Нас)", "3 рет оқылады", 3),
        DhikrItem("Тәубе истиғфары", "100 рет", 100),
        DhikrItem("СубханАллаһ", "33 рет", 33)
    )

    var counter by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().background(TaubeColors.Cream)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = TaubeColors.DarkGreen)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Таңғы зікір", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Күн сайын оқылатын таңғы дұғалар", fontSize = 12.sp, color = Color(0xFFCBD5C0))
            }
        }

        LazyRow(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val index = categories.indexOf(cat)
                val active = selectedCategory == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (active) TaubeColors.DeepGreen else Color(0xFFEFEFEF))
                        .clickable { selectedCategory = index }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(cat, fontSize = 12.sp, color = if (active) Color.White else TaubeColors.TextGray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Таңғы зікірлер",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TaubeColors.TextDark,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(dhikrItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { counter++ },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TaubeColors.TextDark)
                            Text(item.subtitle, fontSize = 11.sp, color = TaubeColors.TextGray)
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(TaubeColors.ActiveBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("${item.target}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TaubeColors.DeepGreen)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================
// 5. ҚОСЫМША (МЕНЮ) ЭКРАНЫ
// =========================================================

@Composable
fun MoreScreen() {
    val menuItems = listOf(
        MoreMenuItem("📅", "Ислам күнтізбесі"),
        MoreMenuItem("🕐", "Ораза уақыты"),
        MoreMenuItem("📖", "Құран оқу жоспары"),
        MoreMenuItem("🎓", "Намазды үйрену"),
        MoreMenuItem("📔", "Хадис күнделігі"),
        MoreMenuItem("🤲", "Дуа кітапшасы"),
        MoreMenuItem("🔖", "Бетбелгілер"),
        MoreMenuItem("⚙️", "Параметрлер")
    )

    Column(modifier = Modifier.fillMaxSize().background(TaubeColors.Cream)) {
        Text(
            text = "Қосымша",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TaubeColors.TextDark,
            modifier = Modifier.padding(20.dp)
        )

        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(menuItems) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.icon, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(item.title, fontSize = 15.sp, color = TaubeColors.TextDark)
                    }
                    Text("›", fontSize = 18.sp, color = TaubeColors.TextGray)
                }
                Divider(color = Color(0xFFF1F5F9))
            }
        }
    }
}

// =========================================================
// КӨМЕКШІ ФУНКЦИЯЛАР ЖӘНЕ КОМПОНЕНТТЕР
// =========================================================

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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (data.isActive) TaubeColors.ActiveBg else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(data.iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = data.name,
                    tint = data.iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = data.name,
                fontSize = 15.sp,
                fontWeight = if (data.isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (data.isActive) TaubeColors.DeepGreen else Color(0xFF1E293B)
            )
        }
        Text(
            text = data.time,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (data.isActive) TaubeColors.DeepGreen else Color(0xFF1E293B)
        )
    }
}
