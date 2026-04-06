package com.user.smartledgerai.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class TransactionNotificationService : NotificationListenerService() {

        override fun onNotificationPosted(sbn: StatusBarNotification?) {
            sbn ?: return

            val packageName = sbn.packageName //发出通知的APP(who)
            val extras = sbn.notification?.extras ?: return //extra意思是通知的内容

            val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

            //Log.d("SmartLedger", "Package: $packageName") UnitTest(Pass)
            //Log.d("SmartLedger", "Title: $title")
            //Log.d("SmartLedger", "Text: $text")
        }

        override fun onNotificationRemoved(sbn: StatusBarNotification?) {
            // 暂时留空
        }
}