package com.example.behaviorsafety

import android.content.ComponentName
import android.content.Context
import android.provider.Settings

object NotificationPermissionUtils {

    fun hasNotificationAccess(context: Context): Boolean {
        val enabledListeners =
            Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false

        val componentName = ComponentName(
            context,
            NotificationMonitorService::class.java
        )

        return enabledListeners.contains(componentName.flattenToString())
    }
}
