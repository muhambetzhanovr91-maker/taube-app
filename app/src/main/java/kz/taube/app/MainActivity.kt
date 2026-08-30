package kz.taube.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TaubeApp()
        }
    }
}

@Composable
fun TaubeApp() {

    val green = Color(0xFF176B4D)
    val background = Color(0xFFF7F8F5)

    var prayerTimes by remember {
        mutableStateOf<DailyPrayerTimes?>(null)
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(Unit) {

        try {

            val result = withContext(Dispatchers.IO) {

                PrayerApi.getPrayerTimes(
                    year = LocalDate.now().year,
                    latitude = 43.4861,
                    longitude = 52.9974
                )
            }

            if (result.isEmpty()) {
                throw Exception("API бос жауап қайтарды")
            }

            val today = LocalDate.now().toString()

            prayerTimes = result.firstOrNull {
                it.date == today
            } ?: result.first()

        } catch (e: Exception) {

            error = "Қате: ${e.message ?: "Белгісіз қате"}"
        }

        loading = false
    }

    MaterialTheme {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(20.dp)
        ) {

            Text(
                text = "TAUBE",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = green
            )

            Text(
                text = "Намаз уақыты — әр күнгі серігің",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            when {

                loading -> {

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator(
                            color = green
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            text = "Намаз уақыттары жүктелуде..."
                        )
                    }
                }

                error != null -> {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF1E9EE)
                        )
                    ) {

                        Text(
                            text = error ?: "",
                            modifier = Modifier.padding(20.dp),
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                    }
                }

                prayerTimes != null -> {

                    val p = prayerTimes!!

                    val prayers = listOf(
                        "Таң" to p.fajr,
                        "Күн шығуы" to p.sunrise,
                        "Бесін" to p.dhuhr,
                        "Екінті" to p.asr,
                        "Ақшам" to p.maghrib,
                        "Құптан" to p.isha
                    )

                    val nextPrayer = findNextPrayer(prayers)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = green
                        )
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "Жаңаөзен",
                                color = Color.White,
                                fontSize = 16.sp
                            )

                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )

                            Text(
                                text = "Келесі намаз",
                                color = Color.White.copy(
                                    alpha = 0.8f
                                )
                            )

                            Text(
                                text = nextPrayer.first,
                                color = Color.White,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = nextPrayer.second,
                                color = Color.White,
                                fontSize = 23.sp
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Countdown(
                                prayerTime = nextPrayer.second
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    Text(
                        text = "Бүгінгі намаз уақыттары",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    prayers.forEach { prayer ->

                        PrayerRow(
                            name = prayer.first,
                            time = prayer.second
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Countdown(
    prayerTime: String
) {

    var remaining by remember {
        mutableStateOf("")
    }

    LaunchedEffect(prayerTime) {

        while (true) {

            try {

                val now = LocalDateTime.now()

                val targetTime =
                    LocalTime.parse(prayerTime)

                var target = LocalDateTime.of(
                    LocalDate.now(),
                    targetTime
                )

                if (!target.isAfter(now)) {
                    target = target.plusDays(1)
                }

                val seconds = Duration.between(
                    now,
                    target
                ).seconds

                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                val secs = seconds % 60

                remaining = String.format(
                    "%02d:%02d:%02d",
                    hours,
                    minutes,
                    secs
                )

            } catch (e: Exception) {

                remaining = "--:--:--"
            }

            delay(1000)
        }
    }

    Text(
        text = "$remaining қалды",
        color = Color.White.copy(alpha = 0.9f),
        fontSize = 16.sp
    )
}

fun findNextPrayer(
    prayers: List<Pair<String, String>>
): Pair<String, String> {

    val now = LocalTime.now()

    return prayers.firstOrNull { prayer ->

        try {
            LocalTime.parse(prayer.second)
                .isAfter(now)
        } catch (e: Exception) {
            false
        }

    } ?: prayers.first()
}

@Composable
fun PrayerRow(
    name: String,
    time: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 18.dp,
                    vertical = 14.dp
                ),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(
                text = name,
                fontSize = 16.sp
            )

            Text(
                text = time,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF176B4D)
            )
        }
    }
}
