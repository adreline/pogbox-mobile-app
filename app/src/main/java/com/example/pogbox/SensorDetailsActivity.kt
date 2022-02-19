package com.example.pogbox

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.google.android.material.bottomsheet.BottomSheetBehavior


class SensorDetailsActivity : AppCompatActivity() {
    lateinit var shared : SharedPreferences//this is a global settings instance

    fun loadImage(uri:Uri,canva:ImageView){
        // Do what you want with it :)
        val requestBuilder: RequestBuilder<PictureDrawable> = GlideToVectorYou
            .init()
            .with(this)
            .requestBuilder
        // Customize request like you were using Glide normally
        requestBuilder
            .load(uri)
            .placeholder(R.drawable.placeholder_for_plot)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(canva)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_details)

        shared = getSharedPreferences("default" , Context.MODE_PRIVATE)//this loads global settings under name of default
        //BottomSheet needs to start in expanded state
        val bottomSheetBehavior: BottomSheetBehavior<*>?
        val bottomSheet: View = findViewById(R.id.constraintLayout)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        val plots_dir = "http://${shared.getString("ADDRESS" , "0.0.0.0" )}${shared.getString("PLOTS_DIR" , "/" )}" //resolve path from settings
        val sensor_discrete_name=intent.getStringExtra("sensor")
        val sensor_friendly_name=shared.getString(sensor_discrete_name,sensor_discrete_name) //load friendly device name, if not set, fallback to discrete device name
        //setup ui
        findViewById<TextView>(R.id.toolbar_title).text="${sensor_friendly_name} - Szczegółowe dane"//set the topbar title
        val plot_canvas_1 = findViewById<ImageView>(R.id.plotCanvas1)
        val plot_canvas_2 = findViewById<ImageView>(R.id.plotCanvas2)
        plot_canvas_1.visibility= View.INVISIBLE
        plot_canvas_2.visibility= View.INVISIBLE
        val name_save_button = findViewById<ImageButton>(R.id.save_name_button)//save device name button
        val name_input = findViewById<EditText>(R.id.device_name_textinput)//TextEdit field with device name
        name_input.setText(sensor_friendly_name) //put fetched name into textinput
        val temp_view = findViewById<TextView>(R.id.temp)//TextView displaying the temp value
        val hum_view = findViewById<TextView>(R.id.hum)//TextView displaying the hum value
        val hum_layout_block = findViewById<LinearLayout>(R.id.hum_layout_block) //Layout block containing humidity display
        val sensor_icon = findViewById<ImageView>(R.id.sensor_icon) //sensor icon to be displayed in a topbar
        //decide which sensor is being shown
        when(sensor_discrete_name){
            "DHT1" -> {
                //glide loading vector images of plots
                loadImage(Uri.parse("${plots_dir}dht1_t_plot.svg"),plot_canvas_1)
                loadImage(Uri.parse("${plots_dir}dht1_h_plot.svg"),plot_canvas_2)
                plot_canvas_1.visibility= View.VISIBLE
                plot_canvas_2.visibility= View.VISIBLE
                val data = intent.getStringExtra("readings")?.split(";")
                temp_view.text = "${data?.get(0)} °C"
                hum_view.text = "${data?.get(1)} %"
                sensor_icon.setImageResource(R.drawable.dht_model)//enable correct icon
            }
            "DHT2" -> {
                //glide loading vector images of plots

                loadImage(Uri.parse("${plots_dir}dht2_t_plot.svg"),plot_canvas_1)
                loadImage(Uri.parse("${plots_dir}dht2_h_plot.svg"),plot_canvas_2)
                plot_canvas_1.visibility= View.VISIBLE
                plot_canvas_2.visibility= View.VISIBLE
                val data = intent.getStringExtra("readings")?.split(";")
                temp_view.text = "${data?.get(0)} °C"
                hum_view.text = "${data?.get(1)} %"
                sensor_icon.setImageResource(R.drawable.dht_model)//enable correct icon
            }
            "DST" -> {
                //glide loading vector images of plots
                loadImage(Uri.parse("${plots_dir}dst_t_plot.svg"),plot_canvas_1)
                plot_canvas_1.visibility= View.VISIBLE
                plot_canvas_2.visibility= View.GONE
                val data = intent.getStringExtra("readings")
                temp_view.text = "${data} °C"
                hum_layout_block.visibility = View.INVISIBLE //hide humidity display
                sensor_icon.setImageResource(R.drawable.dst_model) //enable correct icon
            }
        }

        /*
        This block handles setting custom name for a device
         */
        name_save_button?.setOnClickListener {
            //save name from textinput
            val new_name = name_input.text?.toString()
            val edit = shared.edit() //set settings instance to edit mode
            edit.putString(sensor_discrete_name , new_name) //sensor_discrete_name is a identifier and new_name is a user friendly name
            edit.apply() //save the new name
            showToast("Nazwa zmieniona") //show toast as a feedback from save button
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
            this@SensorDetailsActivity, text,
            Toast.LENGTH_SHORT
        ).show()
    }
}