package com.example.pogbox.growboxapi

import android.content.SharedPreferences

import com.example.pogbox.sensors.DhtModel
import com.example.pogbox.sensors.DstModel
import com.example.pogbox.sensors.ServerInfoModel
import okhttp3.*
import java.io.IOException
import com.google.gson.GsonBuilder


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

    //local variables declarations
    private var dst_state=""
    private var dht_state=""
    private var dht2_state=""
    private var last_updated_dht=""
    private var last_updated_dht2=""
    private var last_updated_dst=""
    private var growlight_state=true
    private var exhaust_state=true
    private var crontab = ""
    private var space_info = ""
    private var cpu_temp_info = ""
    private var database_size = ""

    private val client=OkHttpClient()

    fun updateData(url:String) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gson = GsonBuilder().create()

                when(url){
                    DHT_URL -> {
                        val dht_data: DhtModel = gson.fromJson(body, DhtModel::class.java)
                        dht_state="${dht_data.temperature} °C  ${dht_data.humidity}%"
                        last_updated_dht= "Ostatni update: "+dht_data.time_stamp
                    }
                    DHT2_URL -> {
                        val dht2_data: DhtModel = gson.fromJson(body, DhtModel::class.java)
                        dht2_state="${dht2_data.temperature} °C  ${dht2_data.humidity}%"
                        last_updated_dht2= "Ostatni update: "+dht2_data.time_stamp
                    }
                    DST_URL -> {
                        val dst_data: DstModel = gson.fromJson(body, DstModel::class.java)
                        dst_state="${dst_data.temperature} °C"
                        last_updated_dst = "Ostatni update: "+dst_data.time_stamp
                    }
                    SERVER_INFO -> {
                        val server_info = gson.fromJson(body, ServerInfoModel::class.java)
                        space_info = "Total Size: ${server_info.space_data[0]} Used: ${server_info.space_data[1]} Available: ${server_info.space_data[2]} Use%: ${server_info.space_data[3]}  "
                        cpu_temp_info = "${server_info.cpu_temp} °C"
                        database_size = "${server_info.dbdata} MB"
                    }

                }
            }
        })

    }



    fun setGrowlightState(state:Boolean){
        val trans = if (state) "on" else "off"
        val request = Request.Builder().url(GL_URL+trans).build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
            }
            override fun onResponse(call: Call, response: Response) {
                //some time in future use it to check response (if settings were applied)
                //val body = response.body?.string()
                growlight_state = state
            }
        })
    }
    fun updateGrowlightState(){
        val request = Request.Builder().url(GL_URL+"state").build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                growlight_state = body?.trim()=="0"
            }

        })
    }

    fun setExhaustState(state:Boolean){
        val trans = if (state) "on" else "off"
        val request = Request.Builder().url(EXH_URL+trans).build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
            }
            override fun onResponse(call: Call, response: Response) {
                //some time in future use it to check response (if settings were applied)
                //val body = response.body?.string()
                exhaust_state = state
            }
        })
    }
    fun updateExhaustState(){
        val request = Request.Builder().url(EXH_URL+"state").build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                exhaust_state = body?.trim()=="0"
            }

        })
    }

    fun setGrowlightSchedule(hour_on: String,hour_off: String){
        val a =  hour_on.split(":")
        val b = hour_off.split(":")
        val req = SET_SCHEDULE_URL+"houre=${a.get(0)}&minutee=${a.get(1)}&hourn=${b.get(0)}&minuten=${b.get(1)}"
        val request = Request.Builder().url(req).build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                println(body)
            }

        })
    }
    fun getGrowlightSchedule(){
        val request = Request.Builder().url(GET_SCHEDULE_URL).build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
            }
            override fun onResponse(call: Call, response: Response) {
                crontab = response.body?.string().toString().trim()
            }
        })
    }


    fun getCpuTempInfo(): String{
        return cpu_temp_info
    }
    fun getDatabaseInfo(): String{
        return database_size
    }
    fun getSpaceInfo(): String{
        return space_info
    }
    fun getDht(): String {
        return dht_state
    }
    fun getDht2(): String {
        return dht2_state
    }
    fun getDst(): String {
        return dst_state
    }
    fun getGrowlightState(): Boolean {
        return growlight_state
    }
    fun getExhaustState(): Boolean {
        return exhaust_state
    }
    fun getDhtUpdate(): String{
        return last_updated_dht
    }
    fun getDht2Update(): String{
        return last_updated_dht2
    }
    fun getDstUpdate(): String{
        return last_updated_dst
    }
    fun getCrontab(): String{
        return crontab
    }
}