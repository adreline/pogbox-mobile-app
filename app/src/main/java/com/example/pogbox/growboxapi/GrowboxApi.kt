package com.example.pogbox.growboxapi

import android.content.SharedPreferences
import com.example.pogbox.sensors.*

import okhttp3.*
import java.io.IOException
import com.google.gson.GsonBuilder
import okhttp3.internal.notify


class GrowboxApi(shared: SharedPreferences) {
    //constructor of this class loads server addresses and paths from global settings
    var DHT2_URL = "http://${shared.getString("ADDRESS" , "0.0.0.0" )}${shared.getString("DHT2_URL" , "/" )}"
    var DHT_URL = "http://${shared.getString("ADDRESS" , "0.0.0.0" )}${shared.getString("DHT_URL" , "/" )}"
    var DST_URL = "http://${shared.getString("ADDRESS" , "0.0.0.0" )}${shared.getString("DST_URL" , "/" )}"
    var EXH_URL = "http://${shared.getString("ADDRESS" , "0.0.0.0" )}${shared.getString("EXH_URL" , "/" )}"
    var GET_SCHEDULE_URL = "http://${shared.getString("ADDRESS" , "0.0.0.0" )}${shared.getString("GET_SCHEDULE_URL" , "/" )}"
    var GL_URL = "http://${shared.getString("ADDRESS" , "0.0.0.0" )}${shared.getString("GL_URL" , "/" )}"
    var SERVER_INFO = "http://${shared.getString("ADDRESS" , "0.0.0.0" )}${shared.getString("SERVER_INFO" , "/" )}"
    var SET_SCHEDULE_URL = "http://${shared.getString("ADDRESS" , "0.0.0.0" )}${shared.getString("SET_SCHEDULE_URL" , "/" )}"
    var DAY_AVERAGE_URL = "http://${shared.getString("ADDRESS" , "0.0.0.0" )}${shared.getString("DAY_AVERAGE_URL" , "/" )}"

    //local variables declarations
    private var dst:DstModel? = null
    private var dht1:DhtModel? = null
    private var dht2:DhtModel? = null
    private var server:ServerInfoModel? = null
    private var day_average:DayAverageModel? = null

    private var growlight = GrowlightModel(GL_URL,SET_SCHEDULE_URL,GET_SCHEDULE_URL)
    private var exhaust_fan = ExhaustFanModel(EXH_URL)

    private var connection_state=false


    private val client=OkHttpClient()

    fun updateData(url:String) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                connection_state=false
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gson = GsonBuilder().create()
                connection_state=true
                when(url){
                    DAY_AVERAGE_URL -> {
                        day_average = gson.fromJson(body, DayAverageModel::class.java)
                    }
                    DHT_URL -> {
                        dht1 = gson.fromJson(body, DhtModel::class.java)
                    }
                    DHT2_URL -> {
                        dht2 = gson.fromJson(body, DhtModel::class.java)
                    }
                    DST_URL -> {
                        dst = gson.fromJson(body, DstModel::class.java)
                    }
                    SERVER_INFO -> {
                        server = gson.fromJson(body, ServerInfoModel::class.java)
                    }
                }
            }
        })

    }
    //--------GETTERS-----------
    fun getDht(): DhtModel? {
        return dht1
    }
    fun getDht2(): DhtModel? {
        return dht2
    }
    fun getDst(): DstModel? {
        return dst
    }
    fun getGrowlight(): GrowlightModel {
        return growlight
    }
    fun getServer(): ServerInfoModel?{
        return server
    }
    fun getDayAverage(): DayAverageModel? {
        return day_average
    }
    fun getExhaust(): ExhaustFanModel {
        return exhaust_fan
    }
    fun getConnectionState(): Boolean {
        return connection_state
    }
}