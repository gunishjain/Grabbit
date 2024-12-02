package com.gunishjain.grabbit.internal.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.gunishjain.grabbit.utils.NotificationConst

class DownloadNotificationManager(
    private val context: Context,
    private val notificationConfig: NotificationConfig,
    private val requestId: Int,
    private val fileName: String
) {


    private val notificationBuilder = NotificationCompat.Builder(context, NotificationConst.NOTIFICATION_CHANNEL_ID)
    private val notificationId = requestId

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

    }

    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NotificationConst.NOTIFICATION_CHANNEL_ID,
            notificationConfig.channelName,
            notificationConfig.importance
        )
        channel.description = notificationConfig.channelDescription
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    //Need to add code for notification builder
    //NEED to Implement sending notifications based on status



}