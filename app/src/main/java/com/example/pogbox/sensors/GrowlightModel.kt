package com.example.pogbox.sensors

import okhttp3.*
import java.io.IOException
import java.io.Serializable


data class GrowlightModelState(
    var growlight_state: Boolean?,
    var crontab: String?
) : Serializable
class GrowlightModel (private var GL_URL: String, private var SET_SCHEDULE_URL: String,private var GET_SCHEDULE_URL: String) {
    private val client= OkHttpClient()
    private var growlight = GrowlightModelState(null,null)

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
                growlight.growlight_state = state
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
                growlight.growlight_state = body?.trim()=="0"
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
    fun updateGrowlightSchedule(){
        val request = Request.Builder().url(GET_SCHEDULE_URL).build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
            }
            override fun onResponse(call: Call, response: Response) {
                growlight.crontab = response.body?.string().toString().trim()
            }
        })
    }

    fun getSchedule(): String?{
        return growlight.crontab
    }
    fun getState(): Boolean?{
        return growlight.growlight_state
    }

}