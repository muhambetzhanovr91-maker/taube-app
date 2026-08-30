package kz.taube.app

import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object PrayerApi {

    private const val API_BASE =
        "https://api.muftyat.kz/prayer-times"

    fun getPrayerTimes(
        year: Int,
        latitude: Double,
        longitude: Double
    ): List<DailyPrayerTimes> {

        val url = URL(
            "$API_BASE/$year/$latitude/$longitude"
        )

        val connection = url.openConnection() as HttpURLConnection

        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.useCaches = false
            connection.doInput = true

            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            connection.setRequestProperty(
                "User-Agent",
                "TAUBE Android App"
            )

            val responseCode = connection.responseCode

            if (responseCode !in 200..299) {
                throw IOException(
                    "ҚМДБ API HTTP қате: $responseCode"
                )
            }

            val response = connection.inputStream
                .bufferedReader()
                .use { it.readText() }

            parsePrayerTimes(response)

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
            ?: throw IOException(
                "API ішінде result массиві табылмады"
            )

        for (i in 0 until array.length()) {

            val item = array.getJSONObject(i)

            val date = item.optString(
                "Date",
                ""
            ).trim()

            val fajr = item.optString(
                "fajr",
                ""
            ).trim()

            val sunrise = item.optString(
                "sunrise",
                ""
            ).trim()

            val dhuhr = item.optString(
                "dhuhr",
                ""
            ).trim()

            val asr = item.optString(
                "asr",
                ""
            ).trim()

            val maghrib = item.optString(
                "maghrib",
                ""
            ).trim()

            val isha = item.optString(
                "isha",
                ""
            ).trim()

            if (
                date.isNotEmpty() &&
                fajr.isNotEmpty() &&
                sunrise.isNotEmpty() &&
                dhuhr.isNotEmpty() &&
                asr.isNotEmpty() &&
                maghrib.isNotEmpty() &&
                isha.isNotEmpty()
            ) {
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
        }

        if (result.isEmpty()) {
            throw IOException(
                "ҚМДБ API бос дерек қайтарды"
            )
        }

        return result
    }
}
