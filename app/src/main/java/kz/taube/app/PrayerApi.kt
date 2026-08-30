package kz.taube.app

import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

object PrayerApi {

    fun getPrayerTimes(
        year: Int,
        latitude: Double,
        longitude: Double
    ): List<DailyPrayerTimes> {

        val url = URL(
            "https://api.muftyat.kz/prayer-times/$year/$latitude/$longitude"
        )

        val connection = url.openConnection() as HttpURLConnection

        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val response = connection.inputStream
                .bufferedReader()
                .use { it.readText() }

            parsePrayerTimes(response)
        } finally {
            connection.disconnect()
        }
    }

    private fun parsePrayerTimes(json: String): List<DailyPrayerTimes> {
        val result = mutableListOf<DailyPrayerTimes>()
        val array = JSONArray(json)

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)

            result.add(
                DailyPrayerTimes(
                    date = item.optString("date"),
                    fajr = item.optString("fajr"),
                    sunrise = item.optString("sunrise"),
                    dhuhr = item.optString("dhuhr"),
                    asr = item.optString("asr"),
                    maghrib = item.optString("maghrib"),
                    isha = item.optString("isha")
                )
            )
        }

        return result
    }
}
