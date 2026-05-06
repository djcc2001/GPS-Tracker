package com.embebidos.gpstracker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class RestartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.embebidos.gpstracker.RESTART_SERVICE") {
            val serviceIntent = Intent(context, LocationService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}