package com.example.pogbox

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import com.example.pogbox.growboxapi.ApiScheduler
import com.example.pogbox.growboxapi.GrowboxApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

//class that holds all ui references thrown around
class justAnUi(val api: GrowboxApi,
               val dht_data: TextView,
               val dht2_data: TextView,
               val dst_data: TextView,
               val growlight_shine: ImageView,
               val growlight_switch: Switch,
               val exhaust_fan: ImageView,
               val exhaust_fan_switch:Switch,
               val spin: Animation){

}
class MainActivity : AppCompatActivity() {
    lateinit var shared : SharedPreferences//this is a global settings instance
    //  ---------------DATA DECLARATIONS--------------
    var growlight_switch_flag=false //important sht, basicly when refresh button refreshes switch state, it shouldnt trigger the listener
    var exhaust_switch_flag=false //important sht, basicly when refresh button refreshes switch state, it shouldnt trigger the listener
    //  ---------------ON CREATE (MAIN PROG)--------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        shared = getSharedPreferences("default" , Context.MODE_PRIVATE)//this loads global settings under name of default
        //class that holds all ui references being thrown around
        val just_ui = justAnUi(
            GrowboxApi(shared),
            findViewById<TextView>(R.id.dht_data),
            findViewById<TextView>(R.id.dht_data2),
            findViewById<TextView>(R.id.dst_data),
            findViewById<ImageView>(R.id.growlight_shine),
            findViewById<Switch>(R.id.growlight_switch),
            findViewById<ImageView>(R.id.exhaust_fan_model),
            findViewById<Switch>(R.id.exhaust_fan_switch),
            AnimationUtils.loadAnimation(this,R.anim.spinny)
        )
        val refresh_button = findViewById<FloatingActionButton>(R.id.refresh_button)
        val dht1_model = findViewById<ImageView>(R.id.dht_model)
        val dht2_model = findViewById<ImageView>(R.id.dht_model2)
        val dst_model = findViewById<ImageView>(R.id.dst_model)
        //animate the static fan images
        just_ui.spin.setInterpolator(LinearInterpolator())
        findViewById<ImageView>(R.id.intake_fan_model_1).startAnimation(just_ui.spin)
        findViewById<ImageView>(R.id.intake_fan_model_2).startAnimation(just_ui.spin)
        //toolbar buttons
        val toolbar_settings_button = findViewById<AppCompatImageView>(R.id.toolbar_settings_button)
        //disable back button (we r in main acc)
        findViewById<AppCompatImageView>(R.id.toolbar_back_button).visibility = View.GONE

        //start data refreshing
        val refresher = ApiScheduler(just_ui.api) //use object inside scheduler
        refresher.start() //start refreshing


        //Start ui refreshing coroutine
        CoroutineScope(IO).launch {
            while (isActive){
                runOnUiThread {
                    refreshUi(just_ui)
                }
                delay(500)
            }
        }

        //listeners implementations
        just_ui.growlight_switch.setOnCheckedChangeListener { _, isChecked ->
            //okay so if flag is true it means the listener is suspended
            if(!growlight_switch_flag){
                if (isChecked){
                    //switch growlight on
                    just_ui.api.setGrowlightState(true)
                    just_ui.growlight_shine.visibility=View.VISIBLE
                }else{
                    //switch growlight off
                    just_ui.api.setGrowlightState(false)
                    just_ui.growlight_shine.visibility=View.INVISIBLE
                }

                val message = if (isChecked) "Grow light:ON" else "Grow light:OFF"
                showToast(message)
            }
        }
        just_ui.exhaust_fan_switch.setOnCheckedChangeListener{ _, isChecked ->
            //okay so if flag is true it means the listener is suspended
            if(!exhaust_switch_flag){
                if (isChecked){
                    //switch growlight on
                    just_ui.api.setExhaustState(true)
                    just_ui.exhaust_fan.startAnimation(just_ui.spin)
                }else{
                    //switch growlight off
                    just_ui.api.setExhaustState(false)
                    just_ui.exhaust_fan.clearAnimation()
                }
                val message = if (isChecked) "Wywiew:ON" else "Wywiew:OFF"
                showToast(message)
            }

        }
        //UI listeners
        toolbar_settings_button?.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        refresh_button?.setOnClickListener { _: View ->
            refreshUi(just_ui)
            showToast("Dane zostały odświerzone")
        }
        dht1_model.setOnClickListener {
            val intent = Intent(this, SensorDetailsActivity::class.java)
            intent.putExtra("sensor","DHT1")
            startActivity(intent)
        }
        dht2_model.setOnClickListener {
            val intent = Intent(this, SensorDetailsActivity::class.java)
            intent.putExtra("sensor","DHT2")
            startActivity(intent)
        }
        dst_model.setOnClickListener {
            val intent = Intent(this, SensorDetailsActivity::class.java)
            intent.putExtra("sensor","DST")
            startActivity(intent)
        }
    }
    //  ---------------FUNCTIONS DECLARATIONS--------------
    private fun showToast(text: String){
        Toast.makeText(
            this@MainActivity, text,
            Toast.LENGTH_SHORT
        ).show()
    }
    private fun refreshUi(just_ui: justAnUi){
        just_ui.dht_data.text=just_ui.api.getDht()
        just_ui.dht2_data.text=just_ui.api.getDht2()
        just_ui.dst_data.text=just_ui.api.getDst()
        growlight_switch_flag=true //suspend the switch listener
        exhaust_switch_flag=true //suspend the switch listener
        if(just_ui.api.getExhaustState()){
            just_ui.exhaust_fan.startAnimation(just_ui.spin)
            just_ui.exhaust_fan_switch.isChecked = true
        }else{
            just_ui.exhaust_fan.clearAnimation()
            just_ui.exhaust_fan_switch.isChecked = false
        }

        if(just_ui.api.getGrowlightState()){
            just_ui.growlight_shine.visibility = View.VISIBLE
            just_ui.growlight_switch.isChecked = true
        }else{
            just_ui.growlight_shine.visibility = View.INVISIBLE
            just_ui.growlight_switch.isChecked = false
        }
        growlight_switch_flag=false //unsuspend the switch listener
        exhaust_switch_flag=false //unsuspend the switch listener
    }

}


