package com.user.smartledgerai.utils

import android.content.Context
import android.provider.Settings
import android.util.Log

object PermissionUtils {
    fun isNotificationListenerEnable(context: Context): Boolean {
        val flat: String? = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return flat?.contains(context.packageName) ?: false
    }
}
