package com.example.pogbox

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager

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
        edit.putString("DST_URL" , "/api/getdst.php")
        edit.putString("DHT_URL" , "/api/getdht.php")
        edit.putString("DHT2_URL" , "/api/getdht2.php")
        edit.putString("GL_URL" , "/api/growlight.php?switch=")
        edit.putString("EXH_URL" , "/api/exhaust.php?switch=")
        edit.putString("SET_SCHEDULE_URL" , "/api/makeschedule.php?")
        edit.putString("GET_SCHEDULE_URL" , "/api/getschedule.php")
        edit.putString("SERVER_INFO" , "/api/getsysteminfo.php")
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
        //listen for sound to finish
        mediaPlayer.setOnCompletionListener {
            //sound finished, proceed to main activity
            System.out.println("SOUND END DETECTED")
            mediaPlayer.release()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() //this kills this activity
        }

    }
}