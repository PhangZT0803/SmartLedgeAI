package com.user.smartledgerai.data

import kotlinx.coroutines.flow.Flow
class TransactionRepository(
    private val transactionDao: TransactionDAO,
    private val categoryDao: CategoryDAO,
    private val settingDao: SettingDAO,
    private val allowedApp: AllowedAppDAO
) {

    val getAllTransaction: Flow<List<Transaction>> = transactionDao.getAllTransaction()

    suspend fun insert(transaction: Transaction) = transactionDao.insertTransaction(transaction)
    suspend fun delete(id: Int) = transactionDao.deleteTransaction(id)

    fun search(query: String): Flow<List<Transaction>> = transactionDao.searchTransaction(query)
    fun getByDateRange(start: Long, end: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(start, end)

    fun getTotalExpense(start: Long, end: Long): Flow<Double?> =
        transactionDao.getTotalExpense(start, end)

    fun getAllAllowedApp(): Flow<List<AllowedApp>> = allowedApp.getAllAllowedApp()

    suspend fun insertAllowedApp(app: AllowedApp) = allowedApp.insert(app)
    suspend fun deleteAllowedApp(packageName: String) = allowedApp.delete(packageName)

    suspend fun getAllowedAppList(): List<AllowedApp> = allowedApp.getAllowedAppList()

    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    fun getCategoriesByType(transactionType: TransactionType): Flow<List<Category>> = categoryDao.getCategoriesByType(transactionType)
}