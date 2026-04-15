package com.user.smartledgerai.data

import kotlinx.coroutines.flow.Flow
class AccountRepository(
    private val accountDao: AccountDAO,
    private val settingDao: SettingDAO,
    private val allowedApp: AllowedAppDAO
) {

    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()

    suspend fun insert(account: Account) = accountDao.insertAccount(account)
    suspend fun delete(id: Int) = accountDao.deleteAccount(id)

    fun search(query: String): Flow<List<Account>> = accountDao.searchAccounts(query)
    fun getByDateRange(start: Long, end: Long): Flow<List<Account>> =
        accountDao.getAccountsByDateRange(start, end)

    fun getTotalExpense(start: Long, end: Long): Flow<Double?> =
        accountDao.getTotalExpense(start, end)

    fun getAllAllowedApp(): Flow<List<AllowedApp>> = allowedApp.getAllAllowedApp()

    suspend fun insertAllowedApp(app: AllowedApp) = allowedApp.insert(app)
    suspend fun deleteAllowedApp(packageName: String) = allowedApp.delete(packageName)
}