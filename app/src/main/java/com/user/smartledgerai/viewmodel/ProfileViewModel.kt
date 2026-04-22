package com.user.smartledgerai.viewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.user.smartledgerai.data.TransactionRepository
import com.user.smartledgerai.data.AllowedApp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: TransactionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _installedApp = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApp: StateFlow<List<AppInfo>> = _installedApp

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
            } else {
                pm.getInstalledPackages(PackageManager.GET_META_DATA)
            }
            
            val apps = packages
                .mapNotNull { it.applicationInfo }
                .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
                .map { appInfo ->
                    AppInfo(
                        packageName = appInfo.packageName,
                        appName = pm.getApplicationLabel(appInfo).toString(),
                        icon = pm.getApplicationIcon(appInfo)
                    )
                }
                .sortedBy { it.appName }
            _installedApp.value = apps
        }
    }

    val allowedApps: StateFlow<List<AllowedApp>> = repository.getAllAllowedApp().map { appList ->
        appList.sortedBy { it.packageName }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleAllowedApp(packageName: String, appName: String) {
        viewModelScope.launch {
            val isAllowed = allowedApps.value.any { it.packageName == packageName }
            if (isAllowed) {
                repository.deleteAllowedApp(packageName)
            } else {
                repository.insertAllowedApp(
                    AllowedApp(packageName = packageName, appName = appName)
                )
            }
        }
    }
}
data class AppInfo(val packageName: String, val appName: String,val icon: Drawable?)