package com.example.pogbox

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.example.pogbox.growboxapi.GrowboxApi
import com.example.pogbox.sensors.DstModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import okhttp3.internal.wait

class SettingsActivity : AppCompatActivity() {
    private lateinit var settings : SharedPreferences//this is a global settings instance
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        settings = getSharedPreferences("default" , Context.MODE_PRIVATE)//this loads global settings under name of default
        val api = GrowboxApi(settings) //create service object
        //BottomSheet needs to start in expanded state
        val bottomSheetBehavior: BottomSheetBehavior<*>?
        val bottomSheet: View = findViewById(R.id.bottom_sheet1)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        //toolbars
        val toolbar_back_button = findViewById<AppCompatImageView>(R.id.toolbar_back_button)
        findViewById<AppCompatImageView>(R.id.toolbar_settings_button).visibility = View.GONE
        findViewById<TextView>(R.id.toolbar_title).text="Ustawienia"
        //other
        val save_server_ip_button = findViewById<ImageButton>(R.id.server_address_ip_commit)
        val save_server_port_button = findViewById<ImageButton>(R.id.server_port_commit)

        val ip_input = findViewById<AutoCompleteTextView>(R.id.server_ip_address_input)
        val port_input = findViewById<AutoCompleteTextView>(R.id.server_port_input)

        //enable autocomplete for ip input
        val possible_ips: Array<out String> = resources.getStringArray(R.array.ip_array)
        ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, possible_ips).also { adapter ->
            ip_input.setAdapter(adapter)
        }

        val content3 = findViewById<TextView>(R.id.content3) //server_memory
        val content2 = findViewById<TextView>(R.id.content2) //dht_ambient
        val Content1 = findViewById<TextView>(R.id.Content1) //dst
        val Content2 = findViewById<TextView>(R.id.Content2) //dht_growbox
        val content4 = findViewById<TextView>(R.id.content4) //database_size
        val content5 = findViewById<TextView>(R.id.content5) //cpu_temp
        val content6 = findViewById<TextView>(R.id.content6) //crontab
        //sync data
        api.getGrowlight().updateGrowlightSchedule()
        api.updateData(api.DHT_URL)
        api.updateData(api.DHT2_URL)
        api.updateData(api.DST_URL)
        api.updateData(api.SERVER_INFO)

        //set up UI
        CoroutineScope(IO).launch{
            while (isActive){ //while coroutine is running
                api.getGrowlight().getSchedule()?.let { //only enter this block when data has came
                    runOnUiThread{
                        content6.text = api.getGrowlight().getSchedule()
                    }
                    this.cancel() //data came, close the coroutine
                }
                delay(10)
            }
        }

        CoroutineScope(IO).launch{
            while (isActive){ //while coroutine is running
                api.getServer()?.let { //only enter this block when data has came
                    runOnUiThread{
                        content5.text = api.getServer()?.cpu_temp.toString()
                        content3.text = "Total Size: ${api.getServer()?.space_data?.get(0)} Used: ${api.getServer()?.space_data?.get(1)} Available: ${api.getServer()?.space_data?.get(2)} Use%: ${api.getServer()?.space_data?.get(3)}  "
                        content4.text = api.getServer()?.dbdata.toString()
                    }
                    this.cancel() //data came, close the coroutine
                }
                delay(10)
            }

        }
        CoroutineScope(IO).launch{
            val old_address = settings.getString("ADDRESS" , "0.0.0.0" )
            runOnUiThread{
                ip_input.setText(old_address)
            }
        }
        CoroutineScope(IO).launch{

            while (isActive){ //while coroutine is running
                api.getDst()?.let { //only enter this block when data has came
                    runOnUiThread{ Content1.text = "${api.getDst()?.temperature}\n${api.getDst()?.time_stamp}" }
                    this.cancel() //data came, close the coroutine
                }
                delay(10)
            }
        }
        CoroutineScope(IO).launch{
            while (isActive){ //while coroutine is running
                api.getDht()?.let { //only enter this block when data has came
                    runOnUiThread{ Content2.text = "${api.getDht()?.temperature};${api.getDht()?.humidity}\n${api.getDht()?.time_stamp}" }
                    this.cancel() //data came, close the coroutine
                }
                delay(10)
            }
        }
        CoroutineScope(IO).launch{
            while (isActive){ //while coroutine is running
                api.getDht2()?.let { //only enter this block when data has came
                    runOnUiThread{ content2.text = "${api.getDht2()?.temperature};${api.getDht2()?.humidity}\n${api.getDht2()?.time_stamp}" }
                    this.cancel() //data came, close the coroutine
                }
                delay(10)
            }
        }
        //listeners
        save_server_ip_button?.setOnClickListener{
            val new_server_ip = ip_input.text.toString()
            val edit = settings.edit() //this sets settings instance to edit mode
            edit.putString("ADDRESS",new_server_ip)
            edit.apply() //this saves configuration
            showToast("IP Zapisane")
        }
        save_server_port_button?.setOnClickListener{
            val new_server_port = port_input.text.toString()
            val edit = settings.edit() //this sets settings instance to edit mode
            edit.putString("PORT",new_server_port)
            edit.apply() //this saves configuration
            showToast("Port Zapisany")
        }
        //UI listeners
        toolbar_back_button?.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    private fun showToast(text: String){
        Toast.makeText(
            this@SettingsActivity, text,
            Toast.LENGTH_SHORT
        ).show()
    }

}