package com.gunishjain.grabbit.internal

import com.gunishjain.grabbit.utils.NotificationConst

data class NotificationConfig(
    val enabled: Boolean = NotificationConst.DEFAULT_VALUE_NOTIFICATION_ENABLED,
    val channelName: String = NotificationConst.DEFAULT_VALUE_NOTIFICATION_CHANNEL_NAME,
    val channelDescription: String = NotificationConst.DEFAULT_VALUE_NOTIFICATION_CHANNEL_DESCRIPTION,
    val showSpeed: Boolean = true,
    val showSize: Boolean = true,
    val smallIcon: Int
    )
