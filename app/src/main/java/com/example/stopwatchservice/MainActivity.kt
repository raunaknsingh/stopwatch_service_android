package com.example.stopwatchservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.stopwatchservice.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var _binding : ActivityMainBinding

    private val timeUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == StopwatchService.TIME_UPDATE) {
                val timeInMillis = intent.getLongExtra(StopwatchService.TIME_VALUE, 0L)
                updateUI(timeInMillis)
            }
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        registerReceiver(timeUpdateReceiver, IntentFilter(StopwatchService.TIME_UPDATE))

        with(_binding) {
            startButton.setOnClickListener {
                Intent(this@MainActivity, StopwatchService::class.java).apply {
                    action = "START"
                    startService(this)
                }
            }

            stopButton.setOnClickListener {
                Intent(this@MainActivity, StopwatchService::class.java).apply {
                    action = "STOP"
                    startService(this)
                }
            }

            resetButton.setOnClickListener {
                Intent(this@MainActivity, StopwatchService::class.java).apply {
                    action = "RESET"
                    startService(this)
                }
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(timeUpdateReceiver)
        super.onDestroy()
    }

    private fun updateUI(timeInSec: Long) {
        val sec = timeInSec%60
        val minutes = timeInSec/60
        val hours = minutes/60
        _binding.timeTextView.text = String.format("%02d:%02d:%02d", hours, minutes, sec)
    }
}