package com.example.bysafe

import android.R.id
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT = 0
    private val REQUEST_DISCOVER_BT = 1
    private var mBlueAdapter: BluetoothAdapter? = null
    private lateinit var mOnBtn: Button
    private lateinit var mOffBtn: Button
    private lateinit var mDiscoverBtn: Button
    private lateinit var mPairedBtn: Button
    private lateinit var  mStatusBlueTv: TextView
    private lateinit var mPairedTv: TextView
    private lateinit var mIsBlueOn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mStatusBlueTv = findViewById(R.id.statusBluetoothTv);
        mPairedTv     = findViewById(R.id.pairedTv);
        mOnBtn        = findViewById(R.id.onBtn);
        mOffBtn       = findViewById(R.id.offBtn);
        mDiscoverBtn  = findViewById(R.id.discoverableBtn);
        mPairedBtn    = findViewById(R.id.pairedBtn);
        mIsBlueOn     = findViewById(R.id.IsBlueOnTv);

        //adapter
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();

        //check if bluetooth is available or not
        if (mBlueAdapter == null){
            mStatusBlueTv.setText("Bluetooth is not available");
        }
        else {
            mStatusBlueTv.setText("Bluetooth is available");
        }

        //set image according to bluetooth status(on/off)
        if (mBlueAdapter!!.isEnabled){
            mIsBlueOn.text = "Blutooth is on";
        } else {
            mIsBlueOn.text = "Blutooth is off";
        }

        mOnBtn.setOnClickListener {
                if (!mBlueAdapter!!.isEnabled) {
                    showToast("Turning On Bluetooth...")
                    //intent to on bluetooth
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(intent, REQUEST_ENABLE_BT)
                    mIsBlueOn.text = "Bluetooth is on"
                } else {
                    showToast("Bluetooth is already on")
                }
        }

        //discover bluetooth btn click
        mDiscoverBtn.setOnClickListener {
            if (!mBlueAdapter!!.isDiscovering) {
                showToast("Making Your Device Discoverable")
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                startActivityForResult(intent, REQUEST_DISCOVER_BT)
            }
        }
        //off btn click
        mOffBtn.setOnClickListener {
            if (mBlueAdapter!!.isEnabled) {
                mBlueAdapter!!.disable()
                showToast("Turning Bluetooth Off")
                mIsBlueOn.text = "Bluetooth is off"
            } else {
                showToast("Bluetooth is already off")
            }
        }
        //get paired devices btn click
        mPairedBtn.setOnClickListener {
            if (mBlueAdapter!!.isEnabled) {
                mPairedTv.text = "Paired Devices"
                val devices =
                    mBlueAdapter!!.bondedDevices
                for (device in devices) {
                    mPairedTv.append(
                        """
                            
                            Device: ${device.name}, $device
                            """.trimIndent()
                    )
                }
            } else {
                //bluetooth is off so can't get paired devices
                showToast("Turn on bluetooth to get paired devices")
            }
        }

//        val navView: BottomNavigationView = findViewById(R.id.nav_view)
//
//        val navController = findNavController(R.id.nav_host_fragment)
//        val appBarConfiguration = AppBarConfiguration(setOf(
//                R.id.navigation_scan, R.id.navigation_calibrate))
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)

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

    private fun showToast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
