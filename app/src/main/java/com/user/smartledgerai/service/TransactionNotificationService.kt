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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber


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
    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + job)
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
        Timber.d("NotificationListenerService: Notification received")
        sbn ?: return

        serviceScope.launch {
            try {
                val account = accountDAO.getAccountList()
                val packageName = sbn.packageName
                val allowedList = allowedAppDAO.getAllowedAppList()
                
                if (allowedList.none { it.packageName == packageName }) {
                    Timber.d("Skipped: $packageName not in whitelist")
                    return@launch
                }

                val extras = sbn.notification?.extras ?: return@launch
                val postTime = sbn.postTime
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
                val ticker = sbn.notification?.tickerText?.toString() ?: ""

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
                
                val sourceAccount = accountDAO.getAccountByPackage(packageName)
                transactionDAO.insertTransaction(
                    Transaction(
                        amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                        currency = (data["currency"] as? String) ?: "RM",
                        transactionType = type,
                        merchant = (data["merchant"] as? String) ?: "Unknown",
                        categoryId = -1, // User will verify and assign category later
                        timestamp = sbn.postTime,
                        source = sourceAccount?.accountName ?: packageName,
                        rawData = "$text | $bigText",
                        isVerified = false
                    )
                )
            } catch (e: Exception) {
                Timber.e("Notification processing failed: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}