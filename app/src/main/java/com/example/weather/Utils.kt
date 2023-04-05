package com.example.weather

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class Utils {
    companion object {


        const val BASE_URL="https://api.caiyunapp.com/v2.5/iB6Vo4M8zg6allIy/"
        fun sendHttpRequest(address: String?, listener: HttpCallbackListener) {
            thread {
                var connection: HttpURLConnection? = null
                try {
                    val response = StringBuilder()
                    val url = URL(address)
                    connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 8000
                    connection.readTimeout = 8000
                    val input = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(input))
                    reader.use {
                        reader.forEachLine {
                            response.append(it)
                        }
                    }
                    listener.onFinish(response.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                    listener.onError(e)
                } finally {
                    connection?.disconnect()
                }
            }
        }
    }

}