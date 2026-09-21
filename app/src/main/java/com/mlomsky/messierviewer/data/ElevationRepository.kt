package com.mlomsky.messierviewer.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Looks up ground elevation for a lat/lon via the free Open Topo Data API. */
class ElevationRepository {

    suspend fun lookupMeters(latitudeDeg: Double, longitudeDeg: Double): Double? =
        withContext(Dispatchers.IO) {
            try {
                val url = URL("https://api.opentopodata.org/v1/aster30m?locations=$latitudeDeg,$longitudeDeg")
                (url.openConnection() as HttpURLConnection).run {
                    connectTimeout = 10_000
                    readTimeout = 10_000
                    inputStream.bufferedReader().use { reader ->
                        JSONObject(reader.readText())
                            .getJSONArray("results")
                            .getJSONObject(0)
                            .getDouble("elevation")
                    }
                }
            } catch (_: Exception) {
                null
            }
        }
}
