package com.example.pogbox

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView


class BoxLayoutActivity : AppCompatActivity() {
    private lateinit var settings : SharedPreferences//this is a global settings instance
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_box_layout)
        settings = getSharedPreferences("default" , Context.MODE_PRIVATE)//this loads global settings under name of default
        findViewById<TextView>(R.id.toolbar_title).text="Rozmieszczenie urządzeń"//set the topbar title
        //class that holds all custom labels
        val ui_labels = uiLabels(
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