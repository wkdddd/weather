package com.example.weather

interface HttpCallbackListener {
    fun onFinish(response: String)
    fun onError(e: Exception)
}