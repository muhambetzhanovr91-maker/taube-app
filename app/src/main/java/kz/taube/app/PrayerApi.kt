package kz.taube.app

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object PrayerApi {

    fun getPrayerTimes(
        year: Int,
        latitude: Double,
        longitude: Double
    ): List<DailyPrayerTimes> {

        val address = "https://api.muftyat.kz/prayer-times/$year/$latitude/$longitude"

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
                throw Exception("API қатесі HTTP $code: $response")
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

        // API жауабында "result" немесе "data" массиві болуы мүмкін
        val array = root.optJSONArray("result") 
            ?: root.optJSONArray("data")
            ?: throw Exception("API құрылымында 'result' немесе 'data' массиві табылмады")

        for (i in 0 until array.length()) {

            val item = array.getJSONObject(i)

            // API-дегі кілттердің бас немесе кіші әріппен келуін ескеру
            val date = when {
                item.has("date") -> item.optString("date")
                item.has("Date") -> item.optString("Date")
                else -> ""
            }

            val fajr = item.optString("fajr", item.optString("Fajr", "--:--"))
            val sunrise = item.optString("sunrise", item.optString("Sunrise", "--:--"))
            val dhuhr = item.optString("dhuhr", item.optString("Dhuhr", "--:--"))
            val asr = item.optString("asr", item.optString("Asr", "--:--"))
            val maghrib = item.optString("maghrib", item.optString("Maghrib", "--:--"))
            val isha = item.optString("isha", item.optString("Isha", "--:--"))

            result.add(
                DailyPrayerTimes(
                    date = date,
                    fajr = fajr,
                    sunrise = sunrise,
                    dhuhr = dhuhr,
                    asr = asr,
                    maghrib = maghrib,
                    isha = isha
                )
            )
        }

        return result
    }
}
