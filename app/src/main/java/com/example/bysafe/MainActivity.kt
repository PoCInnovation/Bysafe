package com.example.bysafe

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_scan, R.id.navigation_calibrate))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // get button
        val btn: Button = findViewById<Button>(R.id.mute_button);

        // check if we can change the Ringer Mode (if we can remove do not disturb, ...)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted)
            startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))

        // get AudioManager
        val audio: AudioManager =
            applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // apply correct button text
        btn.text = when (audio.ringerMode) {
            AudioManager.RINGER_MODE_SILENT, AudioManager.RINGER_MODE_VIBRATE -> "unmute"
            else -> "mute"
        }

        // apply button action
        btn.setOnClickListener {
            when (audio.ringerMode) {
                // silent -> normal
                AudioManager.RINGER_MODE_SILENT, AudioManager.RINGER_MODE_VIBRATE -> {
                    audio.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    audio.setStreamVolume(AudioManager.STREAM_RING, audio.getStreamMaxVolume(AudioManager.STREAM_RING), AudioManager.FLAG_SHOW_UI) // could be removed
                    btn.text = "mute"
                }
                // normal -> vibrate
                else -> {
                    audio.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    btn.text = "unmute"
                }
            }
        }
    }
}
