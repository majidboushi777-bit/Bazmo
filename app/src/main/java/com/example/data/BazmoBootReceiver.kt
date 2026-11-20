package com.example.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BazmoBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val cfg = BazmoNotificationHelper.loadBackgroundConfig(context)
            if (cfg != null) {
                BazmoNotificationHelper.scheduleBackgroundCheck(context)
            }
        }
    }
}
