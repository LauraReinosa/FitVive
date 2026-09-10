package com.example.fitvive1.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            com.example.fitvive1.MainActivity.appContext = context.applicationContext
            AndroidNotificationScheduler().rescheduleFromPrefs()
        }
    }
}
