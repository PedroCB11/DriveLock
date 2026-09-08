package com.drivelock.app.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.drivelock.app.DriveLockApplication

class DriveLockNotificationListenerService : NotificationListenerService() {
    override fun onNotificationPosted(notification: StatusBarNotification) {
        val container = (application as DriveLockApplication).container
        if (!container.tripSessionManager.state.value.isActive) return
        if (!container.notificationControlPreferences.isSelected(notification.packageName)) return

        cancelNotification(notification.key)
        container.tripSessionManager.recordBlockedNotification(notification.packageName)
    }
}
