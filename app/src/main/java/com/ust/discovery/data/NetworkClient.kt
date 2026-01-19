package com.ust.discovery.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
}
