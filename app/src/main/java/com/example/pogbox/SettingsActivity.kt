package com.example.pogbox

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import com.example.pogbox.growboxapi.ApiScheduler
import com.example.pogbox.growboxapi.Constants.Companion.DHT2_URL
import com.example.pogbox.growboxapi.Constants.Companion.DHT_URL
import com.example.pogbox.growboxapi.Constants.Companion.DST_URL
import com.example.pogbox.growboxapi.Constants.Companion.SERVER_INFO
import com.example.pogbox.growboxapi.GrowboxApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val api = GrowboxApi() //create service object

        //toolbars
        var toolbar_back_button = findViewById<AppCompatImageView>(R.id.toolbar_back_button)
        findViewById<AppCompatImageView>(R.id.toolbar_settings_button).visibility = View.GONE
        findViewById<TextView>(R.id.toolbar_title).text="Ustawienia"
        //other
        val save_schedule_button = findViewById<Button>(R.id.save_schedule_button)
        val turnon_time = findViewById<EditText>(R.id.turnon_time)
        val turnoff_time = findViewById<EditText>(R.id.turnoff_time)
        val content3 = findViewById<TextView>(R.id.content3)
        val content2 = findViewById<TextView>(R.id.content2)
        val Content1 = findViewById<TextView>(R.id.Content1)
        val Content2 = findViewById<TextView>(R.id.Content2)
        val content4 = findViewById<TextView>(R.id.content4)
        val content5 = findViewById<TextView>(R.id.content5)
        val content6 = findViewById<TextView>(R.id.content6)
        //sync data
        api.getGrowlightSchedule()
        api.updateData(DHT_URL)
        api.updateData(DHT2_URL)
        api.updateData(DST_URL)
        api.updateData(SERVER_INFO)

        //set up UI
        CoroutineScope(IO).launch{
            while(api.getDatabaseInfo()==""){
                delay(10)
            }
            runOnUiThread{ content6.text = api.getDatabaseInfo() }
        }
        CoroutineScope(IO).launch{
            while(api.getCpuTempInfo()==""){
                delay(10)
            }
            runOnUiThread{ content5.text = api.getCpuTempInfo() }
        }
        CoroutineScope(IO).launch{
            while(api.getSpaceInfo()==""){
                delay(10)
            }
            runOnUiThread{ content4.text = api.getSpaceInfo() }
        }
        CoroutineScope(IO).launch{
            while(api.getCrontab()==""){
                delay(10)
            }
            val a = api.getCrontab().replace("pigs w 12 0","").split(" ")
            val ton = a[1].trim()+":"+ a[0].trim()
            val tof = a[6].trim()+":"+ a[5].trim()
            //0 10 * * * 0 22 * * * pigs w 12 1
            runOnUiThread{
                content3.text = api.getCrontab()
                turnon_time.setText(ton)
                turnoff_time.setText(tof)
            }
        }
        CoroutineScope(IO).launch{
            while(api.getDst()==""){
               delay(10)
            }
            runOnUiThread{ Content1.text = api.getDst()+"\n"+api.getDstUpdate() }

        }
        CoroutineScope(IO).launch{
            while(api.getDht()==""){
               delay(10)
            }
            runOnUiThread { Content2.text = api.getDht()+"\n"+api.getDhtUpdate() }
        }
        CoroutineScope(IO).launch{
            while(api.getDht2()==""){
                delay(10)
            }
            runOnUiThread { content2.text = api.getDht2()+"\n"+api.getDht2Update() }
        }
        //listeners
        save_schedule_button?.setOnClickListener{
            val ton = turnon_time.text.toString()
            val tof = turnoff_time.text.toString()
            api.setGrowlightSchedule(ton,tof)
            showToast("Harmonogram wysłany")
        }
        //UI listeners
        toolbar_back_button?.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    fun showToast(text: String){
        Toast.makeText(
            this@SettingsActivity, text,
            Toast.LENGTH_SHORT
        ).show()
    }

}