package com.example.behaviorsafety

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationMonitorService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        val packageName = sbn.packageName ?: return

        if (!BehaviorAnalyzer.isSocialOrChatApp(packageName)) return

        val postTime = sbn.postTime

        NotificationStatsStore.incrementIfNew(
            context = this,
            packageName = packageName,
            postTime = postTime
        )
    }
}
