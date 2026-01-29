package com.example.v2

import android.content.ComponentName
import android.content.Context
import android.provider.Settings

object NotificationPermissionUtils {

    fun hasNotificationAccess(context: Context): Boolean {
        val cn = ComponentName(context, NotificationMonitorService::class.java)
        val flat = cn.flattenToString()

        val enabled = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false

        return enabled.split(":").any { it.contains(cn.packageName) }
    }
}
