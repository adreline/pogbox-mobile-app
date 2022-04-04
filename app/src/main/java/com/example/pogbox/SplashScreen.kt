package com.example.pogbox

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : AppCompatActivity() {
    lateinit var shared : SharedPreferences //this is a global settings instance
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        //setup ringtone player
        var mediaPlayer = MediaPlayer.create(this, R.raw.pogbox_ringtone)
        //this loads global settings under name of default
        shared = getSharedPreferences("default" , Context.MODE_PRIVATE)
        val edit = shared.edit() //this sets settings instance to edit mode
        edit.putString("DST_URL" , "/api/getdst")
        edit.putString("DHT_URL" , "/api/getdht")
        edit.putString("DHT2_URL" , "/api/getdht2")
        edit.putString("DAY_AVERAGE_URL" , "/api?method=day_average")
        edit.putString("GL_URL" , "/api/growlight?switch=")
        edit.putString("EXH_URL" , "/api/exhaust?switch=")
        edit.putString("SET_SCHEDULE_URL" , "/api/growlight/schedule?method=set&")
        edit.putString("GET_SCHEDULE_URL" , "/api/growlight/schedule?method=get")
        edit.putString("SERVER_INFO" , "/api/system")
        edit.putString("PLOTS_DIR" , "/api/plots/")
        edit.apply() //this saves default configuration

        // This is used to hide the status bar and make
        // the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        //everything is prepared
        //play a sound
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
        }
        //This coroutine is a hotfix, for some reason it fixes bug PAA-25
       val cor = CoroutineScope(Dispatchers.IO).launch{
            while(true){
                delay(100)
                System.out.println("${mediaPlayer.currentPosition}")
            }

        }
        //listen for sound to finish
        mediaPlayer.setOnCompletionListener {
            //sound finished, proceed to main activity
            cor.cancel() //kill the coroutine
            mediaPlayer.release()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() //this kills this activity
        }


    }
}