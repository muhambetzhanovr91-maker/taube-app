package kz.taube.app

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar

object PrayerApi {

    fun getPrayerTimes(
        year: Int,
        latitude: Double,
        longitude: Double
    ): List<DailyPrayerTimes> {
        
        // Қазіргі айды алу
        val month = Calendar.getInstance().get(Calendar.MONTH) + 1
        
        // 100% жұмыс істейтін халықаралық Aladhan API (Жаңаөзен координатасымен)
        val address = "https://api.aladhan.com/v1/calendar?latitude=$latitude&longitude=$longitude&method=3&month=$month&year=$year"

        val connection = URL(address).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            val code = connection.responseCode

            val stream = if (code in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val response = stream?.bufferedReader()?.use { it.readText() } ?: ""

            if (code !in 200..299) {
                throw Exception("API қатесі HTTP $code")
            }

            return parsePrayerTimes(response)

        } finally {
            connection.disconnect()
        }
    }

    private fun parsePrayerTimes(
        json: String
    ): List<DailyPrayerTimes> {

        val result = mutableListOf<DailyPrayerTimes>()
        val root = JSONObject(json)
        
        val array = root.optJSONArray("data")
            ?: throw Exception("API-ден 'data' табылмады")

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val timings = item.getJSONObject("timings")
            val dateObj = item.getJSONObject("date").getJSONObject("gregorian")

            // API "31-08-2026" форматында береді, оны MainActivity күтіп тұрған "2026-08-31" форматына ауыстырамыз
            val rawDate = dateObj.getString("date")
            val dateParts = rawDate.split("-")
            val formattedDate = if (dateParts.size == 3) {
                "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}"
            } else rawDate

            // Уақыттың жанындағы "(+05)" деген сияқты артық жазуды алып тастау үшін
            fun cleanTime(time: String): String = time.substringBefore(" ").trim()

            result.add(
                DailyPrayerTimes(
                    date = formattedDate,
                    fajr = cleanTime(timings.optString("Fajr", "--:--")),
                    sunrise = cleanTime(timings.optString("Sunrise", "--:--")),
                    dhuhr = cleanTime(timings.optString("Dhuhr", "--:--")),
                    asr = cleanTime(timings.optString("Asr", "--:--")),
                    maghrib = cleanTime(timings.optString("Maghrib", "--:--")),
                    isha = cleanTime(timings.optString("Isha", "--:--"))
                )
            )
        }

        return result
    }
}
