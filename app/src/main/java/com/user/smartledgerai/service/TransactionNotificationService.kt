package com.user.smartledgerai.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.google.firebase.ktx.Firebase
import com.google.firebase.functions.ktx.functions
import com.user.smartledgerai.data.AllowedAppDAO
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber


data class NotificationParing(
    val packageName: String,
    val postTime: String,
    val text: String,
    val bigText: String,
    val ticker: String
)

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ServiceEntryPoint{
    fun allowedAppDao(): AllowedAppDAO
}
class TransactionNotificationService : NotificationListenerService() {
    private val functions = Firebase.functions
    private lateinit var allowedAppDAO: AllowedAppDAO

    override fun onCreate(){
        super.onCreate()
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ServiceEntryPoint::class.java
        )
        allowedAppDAO = entryPoint.allowedAppDao()
    }
        override fun onNotificationPosted(sbn: StatusBarNotification?) {
            Timber.d("PackageReceive: ${sbn?.packageName}")
            sbn ?: return //android是用java写的,kotlin会对从java来的都做类似 variable! 意思是可能是null也可能是value也可能是什么都没有,必须要做null处理.

            val packageName = sbn.packageName //发出通知的APP(who)
            val extras = sbn.notification?.extras ?: return //extra意思是通知的内容
            val postTime = sbn.postTime //Unix timestamp
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
            val ticker = sbn.notification?.tickerText?.toString() ?: ""//类似滚动的广告灯[HelloWor]->[elloWorl]->[lloWorld]

            val payload = hashMapOf(
                "packageName" to packageName,
                "postTime" to postTime,
                "text" to text,
                "bigText" to bigText,
                "ticker" to ticker
            )

            // Service 里不能直接用 suspend，用 CoroutineScope
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = functions
                        .getHttpsCallable("parseNotification")
                        .call(payload)
                        .await()

                    Timber.d( "Function result: ${result.getData()}")
                } catch (e: Exception) {
                    Timber.e( "Function error: ${e.message}")
                }
            }
        }
}