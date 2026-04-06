package com.user.smartledgerai.data

import kotlinx.coroutines.flow.Flow
class AccountRepository(
    private val accountDao: AccountDAO,
    private val settingDao: SettingDAO
) {

    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()

    suspend fun insert(account: Account) = accountDao.insertAccount(account)
    suspend fun delete(id: Int) = accountDao.deleteAccount(id)

    fun search(query: String): Flow<List<Account>> = accountDao.searchAccounts(query)
    fun getByDateRange(start: Long, end: Long): Flow<List<Account>> =
        accountDao.getAccountsByDateRange(start, end)

    fun getTotalExpense(start: Long, end: Long): Flow<Double?> =
        accountDao.getTotalExpense(start, end)
}