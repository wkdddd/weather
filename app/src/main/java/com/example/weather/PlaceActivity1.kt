package com.example.weather

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.location.LocationClientOption.LocationMode
import com.example.weather.Utils.Companion.sendHttpRequest
import com.google.gson.Gson
import kotlin.math.log


class MainActivity : AppCompatActivity() {
    lateinit var searchEditText: EditText
    lateinit var searchPlaceButton: Button
    var placename = "shanghai"
    private var placeList = ArrayList<Place>()
    val adapter = PlaceAdapter(placeList)

    private val myListener = MyLocationListener()
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.placeactivity1)
        ActivityCollector.addActivity(this)
        searchEditText = findViewById(R.id.searchPlaceEdit)
        searchPlaceButton = findViewById(R.id.searchPlaceButton)
        val layoutManager = LinearLayoutManager(this)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        searchPlaceButton.setOnClickListener {
            //WeatherActivity2.start(this, "SH", 12.34532F)
        }
        val myLocationClient: LocationClient? = null
        myLocationClient!!.registerLocationListener(myListener)
        myLocationClient.start()



        //val searchEditText: EditText = findViewById(R.id.searchPlaceEdit)
        //地名监听器并以此访问服务器
        searchEditText.addTextChangedListener() {
            var context = searchEditText.text.toString().trim()

            if (context.isNotEmpty()) {
                placename =
                    "https://api.caiyunapp.com/v2/place?query=${context}&token=iB6Vo4M8zg6allIy&lang=zh_CN"
                //执行网络操作
                sendHttpRequest(placename, object : HttpCallbackListener {
                    override fun onFinish(response: String) {
                        val weatherPlace = parseJSONWithGSON(response)
                        if (weatherPlace != null) {
                            runOnUiThread()
                            {
                                displayWeatherPlace(weatherPlace)
                                Log.d("PlaceActivity", "it is $context")
                                context = null.toString()
                            }
                        }
                    }

                    override fun onError(e: Exception) {
                        Log.e("PlaceData", "onError: place请求错误")
                    }
                })

            } else {
                Log.d("PlaceActivity1", "context Error!")
            }
        }

    }

    //weatherplace解析出各个地区后用displayweatherhere实现天气
    fun displayWeatherPlace(weatherPlace: WeatherPlace) {
        if (weatherPlace.status != "ok") {
            Log.e("PlaceData", "displayWeatherPlace:status error")
            return
        }
        val places = weatherPlace.places
        adapter.placeList = places
        adapter.notifyDataSetChanged()

    }

    //经纬度json示例https://api.caiyunapp.com/v2.5/iB6Vo4M8zg6allIy/121.489612,31.405457/realtime.json
//地名json示例https://api.caiyunapp.com/v2/place?query=上海大学&token=iB6Vo4M8zg6allIy&lang=zh_CN
//处理json数据
    fun parseJSONWithGSON(jsonData: String): WeatherPlace? {
        val gson = Gson()
        //val weather = gson.fromJson(jsonData, Weather::class.java)
        val weatherPlace = gson.fromJson(jsonData, WeatherPlace::class.java)
        Log.d("PlaceData", "placestatus is ${weatherPlace.status}")
        return weatherPlace
    }
    fun parseJSONWithGSON2(jsonData: String): Weather? {
        val gson = Gson()
        val weather = gson.fromJson(jsonData, Weather::class.java)
        Log.d("PlaceData", "weatherstatus is ${weather.status}")
        return weather
    }
    /**
     *百度地图定位系统
     * 初始化定位参数配置
     */
    private fun ClientOption() {
//定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        val locationClient = LocationClient(applicationContext)
        //声明LocationClient类实例并配置定位参数
        val locationOption = LocationClientOption()
        val myLocationListener = MyLocationListener()
        //注册监听函数
        locationClient.registerLocationListener(myLocationListener)
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        locationOption.locationMode = LocationMode.Hight_Accuracy
        //可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        locationOption.setCoorType("gcj02")
        //可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        locationOption.setScanSpan(1000)
        //可选，设置是否需要地址信息，默认不需要
        locationOption.setIsNeedAddress(true)
        //可选，设置是否需要地址描述
        locationOption.setIsNeedLocationDescribe(true)
        //可选，设置是否需要设备方向结果
        locationOption.setNeedNewVersionRgc(true);
        //可选，设置是否需要最新版本的地址信息。默认需要，即参数为true
        locationOption.setNeedDeviceDirect(false)
        //可选，默认false，设置是否当卫星定位有效时按照1S1次频率输出卫星定位结果
        locationOption.isLocationNotify = true
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        locationOption.setIgnoreKillProcess(true)
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        locationOption.setIsNeedLocationDescribe(true)
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        locationOption.setIsNeedLocationPoiList(true)
        //可选，默认false，设置是否收集CRASH信息，默认收集
        locationOption.SetIgnoreCacheException(false)
        //可选，默认false，设置是否开启卫星定位
        //locationOption.setOpenGnss(true)
        //可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
        locationOption.setIsNeedAltitude(false)
        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
        locationOption.setOpenAutoNotifyMode()
        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
        locationOption.setOpenAutoNotifyMode(3000, 1, LocationClientOption.LOC_SENSITIVITY_HIGHT)
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        locationClient.locOption = locationOption
        //开始定位
        locationClient.start()
    }

    /**
     * 实现定位回调
     */
    class MyLocationListener : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息,更多结果信息获取说明，请参照类参考中BDLocation类中的说明
            //获取纬度信息
            val latitude = location.latitude
            Log.i("MyLocationListener","latitude is ${latitude}")
            //获取经度信息
            val longitude = location.longitude
            Log.i("MyLocationListener","longitude is ${longitude}")
            //获取定位精度，默认值为0.0f
            val radius = location.radius
            Log.i("MyLocationListener","radius is ${radius}")
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
            val coorType = location.coorType
            Log.i("MyLocationListener","coorType is ${coorType}")
            //获取详细地址信息
            val addr = location.addrStr
            Log.i("MyLocationListener","addr is ${addr}")
            //获取国家
            val country = location.country

            val province = location.province
            //获取省份
            val city = location.city
            //获取城市
            val district = location.district
            //获取区县
            val street = location.street
            //获取街道信息
            val adcode = location.adCode
            //获取adcode
            val town = location.town
            //获取乡镇信息
            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
            val errorCode = location.locType
            val intent = Intent("com.example.weather.ACTION_START").apply {
                putExtra(WeatherActivity2.LOCATION_LNG, longitude)
                putExtra(WeatherActivity2.PLACE_NAME, addr)

                putExtra(WeatherActivity2.LOCATION_LAT, latitude)
            }

            //WeatherActivity2.start()
        }

    }

}


