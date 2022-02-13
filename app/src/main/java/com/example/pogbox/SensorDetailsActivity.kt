package com.example.pogbox

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou





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
        val plots_dir = "http://${shared.getString("ADDRESS" , "0.0.0.0" )}${shared.getString("PLOTS_DIR" , "/" )}" //resolve path from settings
        val sensor_name=intent.getStringExtra("sensor")
        //setup ui
        findViewById<TextView>(R.id.toolbar_title).text="${sensor_name} - Szczegółowe dane"
        val plot_canvas_1 = findViewById<ImageView>(R.id.plotCanvas1)
        val plot_canvas_2 = findViewById<ImageView>(R.id.plotCanvas2)
        plot_canvas_1.visibility= View.INVISIBLE
        plot_canvas_2.visibility= View.INVISIBLE
        //decide which sensor is being shown
        when(sensor_name){
            "DHT1" -> {
                //glide loading vector images of plots
                loadImage(Uri.parse("${plots_dir}dht1_t_plot.svg"),plot_canvas_1)
                loadImage(Uri.parse("${plots_dir}dht1_h_plot.svg"),plot_canvas_2)
                plot_canvas_1.visibility= View.VISIBLE
                plot_canvas_2.visibility= View.VISIBLE
            }
            "DHT2" -> {
                //glide loading vector images of plots

                loadImage(Uri.parse("${plots_dir}dht2_t_plot.svg"),plot_canvas_1)
                loadImage(Uri.parse("${plots_dir}dht2_h_plot.svg"),plot_canvas_2)
                plot_canvas_1.visibility= View.VISIBLE
                plot_canvas_2.visibility= View.VISIBLE
            }
            "DST" -> {
                //glide loading vector images of plots
                loadImage(Uri.parse("${plots_dir}dst_t_plot.svg"),plot_canvas_1)
                plot_canvas_1.visibility= View.VISIBLE
                plot_canvas_2.visibility= View.GONE
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
}