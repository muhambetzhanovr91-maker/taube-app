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

        val url = URL(
            "https://api.muftyat.kz/prayer-times/$year/$latitude/$longitude"
        )

        val connection = url.openConnection() as HttpURLConnection

        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "TAUBE/1.0")

            val responseCode = connection.responseCode

            if (responseCode !in 200..299) {
                return emptyList()
            }

            val response = connection.inputStream
                .bufferedReader()
                .use { it.readText() }

            parsePrayerTimes(response)

        } catch (e: Exception) {
            emptyList()
        } finally {
            connection.disconnect()
        }
    }

    private fun parsePrayerTimes(
        json: String
    ): List<DailyPrayerTimes> {

        val result = mutableListOf<DailyPrayerTimes>()

        try {
            val root = JSONObject(json)

            val array = root.optJSONArray("result")
                ?: root.optJSONArray("data")
                ?: return result

            for (i in 0 until array.length()) {

                val item = array.optJSONObject(i)
                    ?: continue

                result.add(
                    DailyPrayerTimes(
                        date = item.optString(
                            "Date",
                            item.optString("date", "")
                        ),
                        fajr = item.optString(
                            "fajr",
                            item.optString("Fajr", "")
                        ),
                        sunrise = item.optString(
                            "sunrise",
                            item.optString("Sunrise", "")
                        ),
                        dhuhr = item.optString(
                            "dhuhr",
                            item.optString("Dhuhr", "")
                        ),
                        asr = item.optString(
                            "asr",
                            item.optString("Asr", "")
                        ),
                        maghrib = item.optString(
                            "maghrib",
                            item.optString("Maghrib", "")
                        ),
                        isha = item.optString(
                            "isha",
                            item.optString("Isha", "")
                        )
                    )
                )
            }

        } catch (e: Exception) {
            return emptyList()
        }

        return result
    }
}
