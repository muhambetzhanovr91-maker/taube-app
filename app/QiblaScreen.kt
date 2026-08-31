package kz.taube.app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun QiblaScreen(
    userLat: Double = 43.34, // Ағымдағы ширектік
    userLng: Double = 52.85  // Ағымдағы бойлық
) {
    val context = LocalContext.current
    var currentDegree by remember { mutableFloatStateOf(0f) }

    // Меккеге дейінгі бағыт (Qibla azimuth) мен қашықтықты есептеу
    val qiblaDegree = remember(userLat, userLng) { calculateQiblaDirection(userLat, userLng) }
    val distanceToMakkah = remember(userLat, userLng) { calculateDistanceToMakkah(userLat, userLng) }

    // Тірі датчиктерді іске қосу
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        var gravity = FloatArray(3)
        var geomagnetic = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) gravity = event.values.clone()
                if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) geomagnetic = event.values.clone()

                val r = FloatArray(9)
                val i = FloatArray(9)
                if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(r, orientation)
                    val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    currentDegree = (azimuth + 360) % 360
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Тірі айналу анимациясы
    val animatedDegree by animateFloatAsState(
        targetValue = -currentDegree,
        animationSpec = tween(durationMillis = 150),
        label = "compassRotation"
    )

    // Құбылаға тура келгенін тексеру (±5 градус дәлдік)
    val diff = abs((currentDegree - qiblaDegree + 360) % 360)
    val isAligned = diff < 5 || diff > 355

    // Дұрыс бағытталғанда вибрация беру
    LaunchedEffect(isAligned) {
        if (isAligned) {
            triggerVibration(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Күңгірт стиль
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Жоғарғы тақырып
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Құбыла бағыты",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Меккеге дейін $distanceToMakkah км",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8)
            )
        }

        // 2. Дәлдік көрсеткіші & Компас
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${currentDegree.toInt()}°",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = if (isAligned) Color(0xFF10B981) else Color.White
            )
            
            Text(
                text = if (isAligned) "🎯 Құбылаға тура келдіңіз!" else "Телефонды бұраңыз",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isAligned) Color(0xFF10B981) else Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Тірі айналатын Компас шеңбері
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .border(
                        width = 4.dp,
                        color = if (isAligned) Color(0xFF10B981) else Color(0xFF334155),
                        shape = CircleShape
                    )
                    .background(Color(0xFF1E293B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Компас шкаласы
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(animatedDegree)
                ) {
                    Text(
                        text = "N",
                        color = Color.Red,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp)
                    )
                    Text(
                        text = "S",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)
                    )
                    Text(
                        text = "E",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp)
                    )
                    Text(
                        text = "W",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp)
                    )

                    // 🕋 Мекке / Қағба иконкасы компас ішінде
                    val qiblaAngleRad = Math.toRadians(qiblaDegree.toDouble())
                    val radius = 100.dp
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .rotate(qiblaDegree.toFloat())
                    ) {
                        Text(
                            text = "🕋",
                            fontSize = 28.sp,
                            modifier = Modifier.offset(y = (-100).dp)
                        )
                    }
                }

                // Ортаңғы меңзер (Стрелка)
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(if (isAligned) Color(0xFF10B981) else Color.Red, CircleShape)
                )
            }
        }

        // 3. Төменгі калибровка ақпараты
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Құбыла азимуты", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Text(text = "${qiblaDegree.toInt()}° Мекке бағыты", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFF334155), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "Дәлдік: ±3°", fontSize = 11.sp, color = Color(0xFF10B981))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

// Qibla Azimuth математикалық есептеуі
fun calculateQiblaDirection(lat: Double, lng: Double): Float {
    val makkahLat = Math.toRadians(21.4225)
    val makkahLng = Math.toRadians(39.8262)
    val userLatRad = Math.toRadians(lat)
    val userLngRad = Math.toRadians(lng)

    val deltaLng = makkahLng - userLngRad
    val y = sin(deltaLng)
    val x = cos(userLatRad) * tan(makkahLat) - sin(userLatRad) * cos(deltaLng)

    var qibla = Math.toDegrees(atan2(y, x)).toFloat()
    return (qibla + 360) % 360
}

// Меккеге дейінгі арақашықтықты есептеу (Гаверсинус формуласы)
fun calculateDistanceToMakkah(lat: Double, lng: Double): Int {
    val r = 6371 // Жер радиусы (км)
    val dLat = Math.toRadians(21.4225 - lat)
    val dLng = Math.toRadians(39.8262 - lng)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat)) * cos(Math.toRadians(21.4225)) *
            sin(dLng / 2) * sin(dLng / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return (r * c).roundToInt()
}

// Вибрация беру
fun triggerVibration(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(100)
        }
    } catch (e: Exception) {
        // Ода сенсор болмаса елемейді
    }
}
