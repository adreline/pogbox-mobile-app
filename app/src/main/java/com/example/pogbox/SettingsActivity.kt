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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var settings : SharedPreferences//this is a global settings instance
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        settings = getSharedPreferences("default" , Context.MODE_PRIVATE)//this loads global settings under name of default
        //

        val api = GrowboxApi(settings) //create service object

        //toolbars
        val toolbar_back_button = findViewById<AppCompatImageView>(R.id.toolbar_back_button)
        findViewById<AppCompatImageView>(R.id.toolbar_settings_button).visibility = View.GONE
        findViewById<TextView>(R.id.toolbar_title).text="Ustawienia"
        //other
        val save_server_ip_button = findViewById<ImageButton>(R.id.server_address_ip_commit)
        val goto_boxlayout_button = findViewById<ImageButton>(R.id.goto_boxlayout_button)
        val ip_input = findViewById<AutoCompleteTextView>(R.id.server_ip_address_input)
        //enable autocomplete for ip input
        val countries: Array<out String> = resources.getStringArray(R.array.ip_array)
        ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, countries).also { adapter ->
            ip_input.setAdapter(adapter)
        }

        findViewById<TextView>(R.id.content3)
        val content2 = findViewById<TextView>(R.id.content2)
        val Content1 = findViewById<TextView>(R.id.Content1)
        val Content2 = findViewById<TextView>(R.id.Content2)
        val content4 = findViewById<TextView>(R.id.content4)
        val content5 = findViewById<TextView>(R.id.content5)
        val content6 = findViewById<TextView>(R.id.content6)
        //sync data
        api.getGrowlightSchedule()
        api.updateData(api.DHT_URL)
        api.updateData(api.DHT2_URL)
        api.updateData(api.DST_URL)
        api.updateData(api.SERVER_INFO)

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
            val old_address = settings.getString("ADDRESS" , "0.0.0.0" )
            runOnUiThread{
                ip_input.setText(old_address)
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
        goto_boxlayout_button?.setOnClickListener{
            val intent = Intent(this, BoxLayoutActivity::class.java)
            startActivity(intent)
        }
        save_server_ip_button?.setOnClickListener{
            val new_server_ip = ip_input.text.toString()
            val edit = settings.edit() //this sets settings instance to edit mode
            edit.putString("ADDRESS",new_server_ip)
            edit.apply() //this saves configuration
            showToast("IP Zapisane")
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