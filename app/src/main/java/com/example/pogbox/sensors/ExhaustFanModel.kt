package com.example.pogbox.sensors

import okhttp3.*
import java.io.IOException
import java.io.Serializable

data class ExhaustFanModelState(
    var state: Boolean?
) : Serializable

class ExhaustFanModel (private var EXH_URL:String){
    private val client= OkHttpClient()
    private var exhaust_fan = ExhaustFanModelState(null)

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
                exhaust_fan.state = state
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
                exhaust_fan.state = body?.trim()=="0"
            }

        })
    }

    fun getState(): Boolean?{
        return exhaust_fan.state
    }

}