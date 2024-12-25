package com.example.stopwatchservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StopwatchService: Service() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var isRunning = false
    private var startTime = 0L
    private lateinit var sharedPreferences : SharedPreferences

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        createNotificationChannel()
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Stopwatch Running")
            .setContentText("The stopwatch service is running.")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()

        startForeground(1, notification) // Start the service in the foreground
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            "START" -> startStopWatch()
            "STOP" -> stopStopWatch()
            "RESET" -> resetStopWatch()
        }

        return START_STICKY
    }

    private fun resetStopWatch() {
        isRunning = false
        startTime = 0L
        sendTimeUpdate(startTime)
        saveUpdatedTimeToPref()
        stopSelf()
    }

    private fun stopStopWatch() {
        isRunning = false
        saveUpdatedTimeToPref()
    }

    private fun startStopWatch() {
        if (!isRunning) {
            isRunning = true
            startTime = sharedPreferences.getLong(TIME_VALUE, 0L)
            coroutineScope.launch {
                while (isRunning) {
                    sendTimeUpdate(startTime)
                    saveUpdatedTimeToPref()
                    startTime++
                    delay(1000)
                }
            }
        }
    }

    private fun sendTimeUpdate(timePassed: Long) {
        Intent(TIME_UPDATE).apply {
            putExtra(TIME_VALUE, timePassed)
            sendBroadcast(this)
        }
    }

    private fun saveUpdatedTimeToPref() {
        sharedPreferences.edit().apply {
            putLong(TIME_VALUE, startTime)
            commit()
        }
    }

    override fun onDestroy() {
        saveUpdatedTimeToPref()
        coroutineScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val TIME_UPDATE = "time_update"
        const val TIME_VALUE = "time_value"
        const val SHARED_PREF = "myPref"
        const val CHANNEL_ID = "ForegroundServiceChannel"

    }
}