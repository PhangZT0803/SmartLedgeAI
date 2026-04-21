package com.user.smartledgerai.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.google.firebase.ktx.Firebase
import com.google.firebase.functions.ktx.functions
import com.user.smartledgerai.data.Account
import com.user.smartledgerai.data.AccountDAO
import com.user.smartledgerai.data.AllowedAppDAO
import com.user.smartledgerai.data.Transaction
import com.user.smartledgerai.data.TransactionDAO
import com.user.smartledgerai.data.TransactionType
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
    fun transactionDao(): TransactionDAO

    fun AccountDao(): AccountDAO
}
class TransactionNotificationService : NotificationListenerService() {
    private val functions = Firebase.functions
    private lateinit var allowedAppDAO: AllowedAppDAO
    private lateinit var transactionDAO: TransactionDAO

    private lateinit var accountDAO: AccountDAO
    override fun onCreate(){
        super.onCreate()
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ServiceEntryPoint::class.java
        )
        allowedAppDAO = entryPoint.allowedAppDao()
        transactionDAO = entryPoint.transactionDao()
        accountDAO = entryPoint.AccountDao()
    }
        override fun onNotificationPosted(sbn: StatusBarNotification?) {
            Timber.d("NotificationListenerService Start")
            sbn ?: return //android是用java写的,kotlin会对从java来的都做类似 variable! 意思是可能是null也可能是value也可能是什么都没有,必须要做null处理.

            // Service 里不能直接用 suspend，用 CoroutineScope
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val account = accountDAO.getAccountList()
                    val packageName = sbn.packageName //发出通知的APP(who)
                    val allowedList = allowedAppDAO.getAllowedAppList()
                    Timber.d("=== Checking whitelist: $allowedList ===")
                    if (allowedList.none { it.packageName == packageName }) {
                        Timber.d("Skipped: $packageName not in whitelist")
                        return@launch
                    }
                    Timber.d("=== PASSED whitelist: $packageName, calling Cloud Function ===")

                    val extras = sbn.notification?.extras ?: return@launch //extra意思是通知的内容
                    val postTime = sbn.postTime //Unix timestamp
                    val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                    val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
                    val ticker = sbn.notification?.tickerText?.toString() ?: ""//类似滚动的广告灯[HelloWor]->[elloWorl]->[lloWorld]

                    val payload = hashMapOf(
                        "packageName" to packageName,
                        "postTime" to postTime,
                        "text" to text,
                        "bigText" to bigText,
                        "ticker" to ticker,
                        "userAccounts" to account.map { it.accountName }
                    )

                    val result = functions
                        .getHttpsCallable("parseNotification")
                        .call(payload)
                        .await()

                    val data = result.getData() as? Map<*, *> ?: return@launch
                    val isTransaction = data["isTransaction"] as? Boolean ?: false
                    if (!isTransaction) return@launch

                    val type = when (data["type"] as? String) {
                        "INCOME" -> TransactionType.INCOME
                        "TRANSFER" -> TransactionType.TRANSFER
                        else -> TransactionType.SPENDING
                    }
                    val sourceAccount = accountDAO.getAccountByPackage(packageName)//找USER自己创建的account
                    transactionDAO.insertTransaction(
                        Transaction(
                            amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                            currency = (data["currency"] as? String) ?: "RM",
                            transactionType = type,
                            merchant = (data["merchant"] as? String) ?: "Unknown",
                            categoryId = -1,//user choose the category in verifyScreen
                            timestamp = sbn.postTime,
                            source = sourceAccount?.accountName ?:packageName,
                            rawData = "$text | $bigText",
                            isVerified = false
                        )
                    )
                    Timber.d( "Function result: ${result.getData()}")
                } catch (e: Exception) {
                    Timber.e( "Function error: ${e.message}")
                }
            }
        }
}