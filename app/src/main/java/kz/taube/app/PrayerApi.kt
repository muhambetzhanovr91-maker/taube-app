package kz.taube.app

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object PrayerApi {

    fun getPrayerTimes(
        year: Int,
        latitude: Double,
        longitude: Double
    ): List<DailyPrayerTimes> {

        val address =
            "https://api.muftyat.kz/prayer-times/$year/$latitude/$longitude"

        val connection = URL(address).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("Accept", "application/json")

            val code = connection.responseCode

            if (code !in 200..299) {
                throw Exception("API қатесі: HTTP $code")
            }

            val response = connection.inputStream
                .bufferedReader()
                .use { it.readText() }

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

        val array = root.optJSONArray("result")
            ?: throw Exception("API result табылмады")

        for (i in 0 until array.length()) {

            val item = array.getJSONObject(i)

            result.add(
                DailyPrayerTimes(
                    date = item.optString("Date"),
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
