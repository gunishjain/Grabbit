package com.gunishjain.grabbit.internal.notifications

import android.content.Context
import android.os.Build
import com.gunishjain.grabbit.internal.NotificationConfig

class DownloadNotificationManager(
    private val context: Context,
    private val notificationConfig: NotificationConfig,
    private val requestId: Int,
    private val fileName: String
) {

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

    }

    private fun createNotificationChannel() {
        TODO("Not yet implemented")
    }

    //Need to add code for notification builder
    //NEED to Implement sending notifications based on status



}