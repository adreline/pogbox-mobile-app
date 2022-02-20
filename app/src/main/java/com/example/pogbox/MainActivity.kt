package com.example.pogbox

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.pogbox.growboxapi.ApiScheduler
import com.example.pogbox.growboxapi.GrowboxApi
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

//this class holds all labels that can be set by user
class uiLabels(val dht1_label: TextView,
               val dht2_label: TextView,
               val dst_label: TextView,
               val lamp_label: TextView,
               val exhaust_label: TextView,
)

//class that holds all ui references thrown around
class justAnUi(val api: GrowboxApi,
               val srednia_temp: TextView,
               val srednia_wilgoc: TextView,
               val lamp_model: ImageView,
               val exhaust_fan: ImageView,
               val spin: Animation,
               val connection_model: ImageView,
                val exhaust_fan_image: ConstraintLayout
               )

class MainActivity : AppCompatActivity() {
    private lateinit var shared : SharedPreferences//this is a global settings instance
    //  ---------------ON CREATE (MAIN PROG)--------------
    @RequiresApi(Build.VERSION_CODES.O) //this is needed for vibrator because its usage diffres depending on android version
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //BottomSheet needs to start in expanded state
        val bottomSheetBehavior: BottomSheetBehavior<*>?
        val bottomSheet: View = findViewById(R.id.constraintLayout2)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        //setup a vibrator
        val v = (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
        shared = getSharedPreferences("default" , Context.MODE_PRIVATE)//this loads global settings under name of default
        //load custom labels
        loadCustomLabels(shared)
        //class that holds all ui references being thrown around
        val just_ui = justAnUi(
            GrowboxApi(shared),
            findViewById(R.id.srednia_temp),
            findViewById(R.id.srednia_wilgoc),
            findViewById(R.id.lamp_model_2),
            findViewById(R.id.exhaust_fan_model),
            AnimationUtils.loadAnimation(this,R.anim.spinny),
            findViewById(R.id.connection_model),
            findViewById(R.id.exhaust_fan)
        )

        val refresh_button = findViewById<FloatingActionButton>(R.id.refresh_button)
        val dht1_button = findViewById<ConstraintLayout>(R.id.dht1_button)
        val dht2_button = findViewById<ConstraintLayout>(R.id.dht2_button)
        val dst_button = findViewById<ConstraintLayout>(R.id.dst_button)
        val exhaust_settings_button = findViewById<LinearLayout>(R.id.exhaust_settings_button)
        val lamp_settings_button = findViewById<LinearLayout>(R.id.lamp_settings_button)
        val server_status_button = findViewById<LinearLayout>(R.id.server_status_button)
        //animate the static fan images
        just_ui.spin.interpolator = LinearInterpolator()
        //toolbar buttons
        val toolbar_settings_button = findViewById<AppCompatImageView>(R.id.toolbar_settings_button)
        //disable back button (we r in main acc)
        findViewById<AppCompatImageView>(R.id.toolbar_back_button).visibility = View.GONE
        //start data refreshing
        val refresher = ApiScheduler(just_ui.api) //use api object inside scheduler
        refresher.start() //start refreshing
        //refresh DayAverage just once here and not in while(isActive) loop (no need to stress mysql db)
        just_ui.api.updateData(just_ui.api.DAY_AVERAGE_URL)
        //Start ui refreshing coroutine
        CoroutineScope(IO).launch {
            while (isActive){
                runOnUiThread {
                    refreshUi(just_ui)
                }
                delay(500)
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
        dht1_button.setOnClickListener {
            val intent = Intent(this, SensorDetailsActivity::class.java)
            intent.putExtra("sensor","DHT1")
            intent.putExtra("readings",just_ui.api.getDht())
            startActivity(intent)
        }
        dht2_button.setOnClickListener {
            val intent = Intent(this, SensorDetailsActivity::class.java)
            intent.putExtra("sensor","DHT2")
            intent.putExtra("readings",just_ui.api.getDht2())
            startActivity(intent)
        }
        dst_button.setOnClickListener {
            val intent = Intent(this, SensorDetailsActivity::class.java)
            intent.putExtra("sensor","DST")
            intent.putExtra("readings",just_ui.api.getDst())
            startActivity(intent)
        }
        exhaust_settings_button?.setOnClickListener{
            val intent = Intent(this, DeviceDetailsActivity::class.java)
            intent.putExtra("device","EXHAUST")
            startActivity(intent)
        }
        lamp_settings_button?.setOnClickListener{
            val intent = Intent(this, DeviceDetailsActivity::class.java)
            intent.putExtra("device","LAMP")
            startActivity(intent)
        }
        server_status_button?.setOnClickListener{
            showToast("Ostatnia aktualizacja: ${just_ui.api.getDht()?.time_stamp}")
        }
        //this allows for devices to be steered by long pressing their icons
        exhaust_settings_button?.setOnLongClickListener {
            if (just_ui.api.getExhaust().getState()!!){
                //switch exhaust off
                just_ui.api.getExhaust().setExhaustState(false)
                showToast("${shared.getString("EXHAUST" , "EXHAUST" )} OFF")
            }else{
                //switch exhaust on
                just_ui.api.getExhaust().setExhaustState(true)
                showToast("${shared.getString("EXHAUST" , "EXHAUST" )} ON")
            }
            v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            true }
        lamp_settings_button?.setOnLongClickListener {

                if (just_ui.api.getGrowlight().getState()!!){
                    //switch growlight off
                    just_ui.api.getGrowlight().setGrowlightState(false)
                    showToast("${shared.getString("LAMP" , "LAMP" )} OFF")
                }else{
                    //switch growlight on
                    just_ui.api.getGrowlight().setGrowlightState(true)
                    showToast("${shared.getString("LAMP" , "LAMP" )} ON")
                }


            v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            true }

    }
    //  ---------------FUNCTIONS DECLARATIONS--------------
    private fun showToast(text: String){
        Toast.makeText(
            this@MainActivity, text,
            Toast.LENGTH_SHORT
        ).show()
    }
    private fun refreshUi(just_ui: justAnUi){
        //updating text data
                just_ui.api.getDayAverage()?.let {
                    runOnUiThread{
                        //strip unnecessery float precision
                        val avt = String.format("%.2f", just_ui.api.getDayAverage()?.avt)
                        val avh = String.format("%.2f", just_ui.api.getDayAverage()?.avh)
                        just_ui.srednia_temp.text= "${avt} °C"
                        just_ui.srednia_wilgoc.text= "${avh} %"
                    }
                }

        //update connection status
        if(just_ui.api.getConnectionState()){
            just_ui.connection_model.setImageResource(R.drawable.signal_online)
        }else{
            just_ui.connection_model.setImageResource(R.drawable.signal_offline)
        }
        //updating fan animations
        just_ui.api.getExhaust().getState()?.let {
            if(just_ui.api.getExhaust().getState()!!){
                just_ui.exhaust_fan_image.alpha=1.0.toFloat()
                just_ui.exhaust_fan.startAnimation(just_ui.spin)
            }else{
                just_ui.exhaust_fan_image.alpha=1.0.toFloat()
                just_ui.exhaust_fan.clearAnimation()
            }
        }
        just_ui.api.getGrowlight().getState()?.let {
            //updating lamp image to match it's state
            if(just_ui.api.getGrowlight().getState()!!){
                just_ui.lamp_model.alpha=1.0.toFloat()
                just_ui.lamp_model.setImageResource(R.drawable.lamp_model_on)
            }else{
                just_ui.lamp_model.alpha=1.0.toFloat()
                just_ui.lamp_model.setImageResource(R.drawable.lamp_model_off)
            }
        }

    }
    private fun loadCustomLabels(settings: SharedPreferences){
        //class that holds all custom labels
        var ui_labels = uiLabels(
            findViewById(R.id.dht1_name),
            findViewById(R.id.dht2_name),
            findViewById(R.id.dst_name),
            findViewById(R.id.lamp_name),
            findViewById(R.id.exhaust_name)
        )
        //set all device names according to user settings
        ui_labels.dht1_label.text= settings.getString("DHT1" , "DHT1" )
        ui_labels.dht2_label.text= settings.getString("DHT2" , "DHT2" )
        ui_labels.dst_label.text= settings.getString("DST" , "DST" )
        ui_labels.exhaust_label.text= settings.getString("EXHAUST" , "EXHAUST" )
        ui_labels.lamp_label.text= settings.getString("LAMP" , "LAMP" )

        ui_labels = uiLabels(
            findViewById(R.id.dht1_textView14),
            findViewById(R.id.dht2_textView8),
            findViewById(R.id.dst_textView16),
            findViewById(R.id.growbox_textView9),
            findViewById(R.id.exhaust_textView11)
        )
        //set all device names according to user settings
        ui_labels.dht1_label.text= settings.getString("DHT1" , "DHT1" )
        ui_labels.dht2_label.text= settings.getString("DHT2" , "DHT2" )
        ui_labels.dst_label.text= settings.getString("DST" , "DST" )
        ui_labels.exhaust_label.text= settings.getString("EXHAUST" , "EXHAUST" )
        ui_labels.lamp_label.text= settings.getString("LAMP" , "LAMP" )

    }

}


