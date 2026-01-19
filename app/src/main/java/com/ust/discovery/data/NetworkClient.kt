package com.ust.discovery.data

import android.util.Log
import com.ust.discovery.domain.IpGeoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object NetworkClient {

    private const val TAG = "NetworkClient"

    suspend fun get(urlString: String): Result<String> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                Log.d(TAG, "GET request to $urlString successful")
                Result.success(response.toString())
            } else {
                Log.e(TAG, "GET request to $urlString failed with response code: $responseCode")
                Result.failure(Exception("HTTP $responseCode"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "GET request to $urlString failed", e)
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }

    suspend fun getIpGeoInfo(ip: String): Result<IpGeoInfo> = withContext(Dispatchers.IO) {
        try {
            val result = get("https://ipinfo.io/$ip/geo")
            result.fold(
                onSuccess = { jsonString ->
                    val json = JSONObject(jsonString)
                    val geoInfo = IpGeoInfo(
                        ip = json.optString("ip", ""),
                        city = json.optString("city", ""),
                        region = json.optString("region", ""),
                        country = json.optString("country", ""),
                        loc = json.optString("loc", ""),
                        org = json.optString("org", ""),
                        timezone = json.optString("timezone", "")
                    )
                    Log.d(TAG, "Geo info parsed successfully for IP: $ip")
                    Result.success(geoInfo)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to get geo info for IP: $ip", error)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing geo info for IP: $ip", e)
            Result.failure(e)
        }
    }
}
