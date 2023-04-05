package com.example.weather

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.weather.Utils.Companion.BASE_URL
import com.example.weather.Utils.Companion.sendHttpRequest
import com.google.gson.Gson

private const val TAG = "WeatherActivity2"

class WeatherActivity2 : AppCompatActivity() {
    private var astroList = ArrayList<Astro>()
    private var temList = ArrayList<Temperature>()
    private var skyList = ArrayList<Skycon>()
    private var lifeIndex = ArrayList<LifeIndex>()
    lateinit var backButton: Button
    lateinit var forecastLayout: LinearLayout
    lateinit var changeButton: Button
    lateinit var placeName: TextView
    lateinit var currentTemp: TextView
    lateinit var currentSky: TextView
    lateinit var currentAqi: TextView
    lateinit var weatherBackground:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weatheractivity2)
        ActivityCollector.addActivity(this)
        changeButton = findViewById(R.id.button_change)
        weatherBackground=findViewById(R.id.weatherBackground)
        backButton = findViewById(R.id.button_back)
        forecastLayout = findViewById(R.id.forecastLayout)
        placeName = findViewById(R.id.placeName)
        currentTemp = findViewById(R.id.currentTemp)
        currentSky = findViewById(R.id.currentSky)
        currentAqi = findViewById(R.id.currentAQI)
        val locationLng = intent.getDoubleExtra(LOCATION_LNG, -1.0)
        val locationLat = intent.getDoubleExtra(LOCATION_LAT, -1.0)
        placeName.text = intent.getStringExtra(PLACE_NAME)
        backButton.setOnClickListener {
            finish()
        }

        if (locationLat <= 0 || locationLng <= 0) {
            Log.e(TAG, "onCreate: 经纬度为负数")
            finish()
        }
        val nowPlaceJson =
            BASE_URL + "${locationLng},${locationLat}/realtime.json"
        val futurePlaceJson =
            BASE_URL + "${locationLng},${locationLat}/daily.json"
        Log.e(this.toString(), "furture$futurePlaceJson")
        Log.e(this.toString(), "now$nowPlaceJson")
        //执行网络操作,传入displayweather
        //future
        sendHttpRequest(futurePlaceJson, object : HttpCallbackListener {

            override fun onFinish(response: String) {
                val weather = parseJSONWithGSON(response)
                if (weather != null) {
                    runOnUiThread()
                    {
                        displayFutureWeather(weather)
                    }
                }
            }

            override fun onError(e: Exception) {
                Log.e("PlaceData", "onError: place请求错误")
            }
        })
        //now
        sendHttpRequest(nowPlaceJson, object : HttpCallbackListener {

            override fun onFinish(response: String) {
                val weather = parseJSONWithGSON(response)
                if (weather != null) {
                    runOnUiThread()
                    {
                        displayNowWeather(weather)
                    }
                }
            }

            override fun onError(e: Exception) {
                Log.e("PlaceData", "onError: place请求错误")
            }
        })
    }

    private fun displayNowWeather(weather: Weather) {
        if (weather.status != "ok") {
            Log.e("PlaceData", "displayFutureWeatherPlace:status error")
            return
        }
        val realtime = weather.result.realtime
        val sky = getSky(realtime.skycon)
        currentAqi.text = realtime.air_quality.aqi.chn.toString()
        currentTemp.text = realtime.temperature.toString()
        currentSky.text = sky.skycon
        Glide.with(this).load(sky.background).into(weatherBackground)//设置实时天气图片
    }

    private fun displayFutureWeather(weather: Weather) {
        if (weather.status != "ok") {
            Log.e("PlaceData", "displayNowWeatherPlace:status error")
            return
        }
        val daily = weather.result.daily


        //填充forecast中的数据
        //forecastLayout.removeAllViews()

        val days = daily.skycon.size
        // 自己写一个类，1天 Day 情况
        // for 里构造一个list更新recycler view
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view =
                LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false)
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView//天气情况
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView//日期
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView//天气图片
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView//温度
            val tempText = "${temperature.min.toInt()}~${temperature.max.toInt()}"
            val sky = getSky(skycon.value)//转换value
            Glide.with(this).load(sky.skyIcon).into(skyIcon)//设置天气图片

            skyInfo.text = sky.skycon//设置天气情况
            dateInfo.text = temperature.date.substring(0, 10)//设置日期
            temperatureInfo.text = tempText//设置温度
            forecastLayout.addView(view)
        }
    }

    fun parseJSONWithGSON(jsonData: String): Weather? {
        val gson = Gson()
        val weather = gson.fromJson(jsonData, Weather::class.java)
        Log.d("PlaceData", "weatherstatus is ${weather.status}")
        return weather
    }

    val sky = mapOf(
        "CLEAR_DAY" to Sky("晴", R.drawable.sun, R.drawable.sunnybg),
        "CLEAR_NIGHT" to Sky("晴", R.drawable.sun, R.drawable.sunnybg),
        "PARTLY_CLOUDY_DAY" to Sky("多云", R.drawable.cloudy, R.drawable.cloudybg),
        "PARTLY_CLOUDY_NIGHT" to Sky("多云", R.drawable.cloudy, R.drawable.cloudybg),
        "CLOUDY" to Sky("阴", R.drawable.cloudy, R.drawable.cloudybg),
        "WIND" to Sky("大风", R.drawable.wind, R.drawable.sunnybg),
        "LIGHT_RAIN" to Sky("小雨", R.drawable.rain, R.drawable.rainbg),
        "MODERATE_RAIN" to Sky("中雨", R.drawable.rain, R.drawable.rainbg),
        "HEAVY_RAIN" to Sky("大雨", R.drawable.rain, R.drawable.rainbg),
        "STORM_RAIN" to Sky("暴雨", R.drawable.thunder, R.drawable.rainbg),
        "THUNDER_SHOWER" to Sky("雷阵雨", R.drawable.thunder, R.drawable.rainbg),
        "SLEET" to Sky("雨夹雪", R.drawable.hatil, R.drawable.snowbg),
        "LIGHT_SNOW" to Sky("小雪", R.drawable.snow, R.drawable.snowbg),
        "MODERATE_SNOW" to Sky("中雪", R.drawable.snow, R.drawable.snowbg),
        "HEAVY_SNOW" to Sky("大雪", R.drawable.snow, R.drawable.snowbg),
        "STORM_SNOW" to Sky("暴雪", R.drawable.snow, R.drawable.snowbg),
        "HAIL" to Sky("冰雹", R.drawable.hatil, R.drawable.snowbg),
        "LIGHT_HAZE" to Sky("轻度雾霾", R.drawable.fog, R.drawable.cloudybg),
        "MODERATE_HAZE" to Sky("中度雾霾", R.drawable.fog, R.drawable.cloudybg),
        "HEAVY_HAZE" to Sky("重度雾霾", R.drawable.fog, R.drawable.cloudybg),
        "FOG" to Sky("雾", R.drawable.fog, R.drawable.cloudybg),
        "DUST" to Sky("浮尘", R.drawable.hatil, R.drawable.cloudybg)
    )//转换得到的天气状况为相应的图片文字等

    fun getSky(skycon: String): Sky {
        return sky[skycon] ?: sky["CLEAR_DAY"]!!
    }

    companion object {
        const val LOCATION_LNG = "location_lng"
        const val PLACE_NAME = "place_name"
        const val LOCATION_LAT = "location_lat"
        fun start(context: Context, city: String, lg: Float, temperature: Double) {
            val starter = Intent(context, WeatherActivity2::class.java)
                .putExtra("city2", city)
                .putExtra("lg", lg)
                .putExtra("name", 123)
            context.startActivity(starter)
        }
    }
}