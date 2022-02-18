package com.example.pogbox

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.pogbox.growboxapi.ApiScheduler
import com.example.pogbox.growboxapi.GrowboxApi
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.*

class DeviceDetailsActivity : AppCompatActivity() {
    lateinit var shared : SharedPreferences//this is a global settings instance
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_details)
        /*
        This class is handling all devices that can be controlled, like fans and lamps
         */
        shared = getSharedPreferences("default" , Context.MODE_PRIVATE)//this loads global settings under name of default
        val api = GrowboxApi(shared) //create api service object
        //BottomSheet needs to start in expanded state
        val bottomSheetBehavior: BottomSheetBehavior<*>?
        val bottomSheet: View = findViewById(R.id.constraintLayout6)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        //load settings
        val device_discrete_name=intent.getStringExtra("device")
        val device_friendly_name=shared.getString(device_discrete_name,device_discrete_name) //load friendly device name, if not set, fallback to discrete device name
        findViewById<TextView>(R.id.toolbar_title).text="${device_friendly_name} - Sterowanie urządzeniem"//set the topbar title
        var listener_flag = false //this flag blocks switch listener from triggering when ui coroutine updates the UI
        //UI elements declarations
        val device_name_input = findViewById<EditText>(R.id.device_name_input)
        val device_name_save_button = findViewById<ImageButton>(R.id.save_name_button_2)
        val device_switch = findViewById<Switch>(R.id.device_switch)
        val schedule_save_button = findViewById<Button>(R.id.save_schedule_button_2)
        val schedule_from_input = findViewById<EditText>(R.id.from_time_input)
        val schedule_untill_input = findViewById<EditText>(R.id.untill_time_input)
        val lamp_image = findViewById<ImageView>(R.id.lamp_model)
        val fan_blades_image = findViewById<ImageView>(R.id.fan_blades)
        val fan_chassi_image = findViewById<ImageView>(R.id.fan_chassi)
        val schedule_input_layout = findViewById<LinearLayout>(R.id.schedule_input_layout)
        val harmonogram_label = findViewById<TextView>(R.id.harmonogram_label_1)
        val sub_window = findViewById<ConstraintLayout>(R.id.sub_window)
        val spin = AnimationUtils.loadAnimation(this,R.anim.spinny)
        spin.setInterpolator(LinearInterpolator())
        //start data refreshing
        val refresher = ApiScheduler(api) //use api object inside scheduler
        refresher.start() //start refreshing data
        api.getGrowlightSchedule() //fetch crontab info
        device_name_input.setText(device_friendly_name)
        //setup UI according to which is chosen
        when(device_discrete_name){
            "LAMP"->{
                lamp_image.visibility = View.VISIBLE //display lamp image
                //add click listener for saving lamp schedule
                System.out.println("INSIDE LAMP CASE")
                schedule_save_button?.setOnClickListener {
                    //send new lamp schedule
                    val ton = schedule_from_input.text.toString()
                    val tof = schedule_untill_input.text.toString()
                    api.setGrowlightSchedule(ton,tof)
                    showToast("Harmonogram wysłany")
                }
                //Start ui refreshing coroutines
                //crontab data
                CoroutineScope(Dispatchers.IO).launch {
                    System.out.println("INSIDE LAMP UI COROUTINE ")
                    while (isActive){
                        //wait for api data to come
                        while(api.getCrontab()==""){
                            System.out.println("INSIDE LAMP UI COROUTINE DATA LOOP")
                            delay(10)
                        }
                        runOnUiThread {
                            System.out.println("INSIDE LAMP UI COROUTINE UITHREAD")
                            //update crontab time table inside text input
                            val a = api.getCrontab().replace("pigs w 12 0","").split(" ")
                            val ton = a[1].trim()+":"+ a[0].trim()
                            val tof = a[6].trim()+":"+ a[5].trim()
                            schedule_from_input.setText(ton)
                            schedule_untill_input.setText(tof)
                        }
                        delay(500)
                    }
                }
                //growlight state
                CoroutineScope(Dispatchers.IO).launch {
                    while (isActive){
                        //wait for api data to come
                        runOnUiThread {
                            //update lamp image state
                            if(api.getGrowlightState()){
                                //set image drawable from lamp OFF to lamp ON
                                lamp_image.setImageResource(R.drawable.lamp_model_on)
                                //set switch to true
                                listener_flag=true //block switch state listener
                                device_switch.isChecked = true
                                listener_flag=false
                            }else{
                                //set image drawable from lamp ON to lamp OFF
                                lamp_image.setImageResource(R.drawable.lamp_model_off)
                                //set switch to false
                                listener_flag=true //block switch state listener
                                device_switch.isChecked = false
                                listener_flag=false
                            }
                        }
                        delay(500)
                    }
                }

            }
            "EXHAUST"->{
                fan_blades_image.visibility = View.VISIBLE //display fan_blades_image image
                fan_chassi_image.visibility = View.VISIBLE //display fan_chassi_image image
                //remove scheduling, exhaust fan doesn't support it
                schedule_input_layout.visibility = View.GONE
                harmonogram_label.visibility = View.GONE
                sub_window.visibility = View.GONE
                //Start ui refreshing coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    while (isActive){
                        runOnUiThread {
                            if(api.getExhaustState()){
                                //Start spinning animation, the fan is ON
                                fan_blades_image.startAnimation(spin)
                                //set switch to true
                                listener_flag=true //block switch state listener
                                device_switch.isChecked = true
                                listener_flag=false
                            }else{
                                //Stop spinning animation, the fan is OFF
                                fan_blades_image.clearAnimation()
                                //set switch to false
                                listener_flag=true //block switch state listener
                                device_switch.isChecked = false
                                listener_flag=false
                            }
                        }
                        delay(500)
                    }
                }

            }
        }





        //listeners implementations
        //This saves custom device name
        device_name_save_button?.setOnClickListener {
            //save name from textinput
            val new_name = device_name_input.text?.toString()
            val edit = shared.edit() //set settings instance to edit mode
            edit.putString(device_discrete_name , new_name) //sensor_discrete_name is a identifier and new_name is a user friendly name
            edit.apply() //save the new name
            showToast("Nazwa zmieniona") //show toast as a feedback from save button
        }
        //device switch just runs a function that corresponds to device name passed via intent
        device_switch.setOnCheckedChangeListener { _, isChecked ->
            if (!listener_flag){
                when(device_discrete_name){
                    "LAMP"->{
                        growlightSwitch(isChecked,api)
                    }
                    "EXHAUST"->{
                        exhaustfanSwitch(isChecked,api)
                    }
                }
            }
        }
        //toolbar handling
        val toolbar_back_button = findViewById<AppCompatImageView>(R.id.toolbar_back_button)
        val toolbar_settings_button = findViewById<AppCompatImageView>(R.id.toolbar_settings_button)

        toolbar_back_button?.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        toolbar_settings_button?.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }



    private fun showToast(text: String){
        Toast.makeText(
            this@DeviceDetailsActivity, text,
            Toast.LENGTH_SHORT
        ).show()
    }
    /*
    This functions perform api calls and ui changes according to user input
    * */
    private fun growlightSwitch(isChecked:Boolean,api:GrowboxApi){
        if (isChecked){
            //switch growlight on
            api.setGrowlightState(true)
            //growlight_shine.visibility= View.VISIBLE
        }else{
            //switch growlight off
            api.setGrowlightState(false)
            //growlight_shine.visibility= View.INVISIBLE
        }

        val message = if (isChecked) "Grow light:ON" else "Grow light:OFF"
        showToast(message)
    }
    private fun exhaustfanSwitch(isChecked:Boolean,api:GrowboxApi){
        if (isChecked){
            //switch growlight on
            api.setExhaustState(true)
            //exhaust_fan.startAnimation(just_ui.spin)
        }else{
            //switch growlight off
            api.setExhaustState(false)
            //exhaust_fan.clearAnimation()
        }
        val message = if (isChecked) "Wywiew:ON" else "Wywiew:OFF"
        showToast(message)
    }
}