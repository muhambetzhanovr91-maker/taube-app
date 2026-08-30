package kz.taube.app

data class PrayerTime(
    val name: String,
    val time: String
)

data class DailyPrayerTimes(
    val date: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)
