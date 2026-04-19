package com.user.smartledgerai.viewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
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
class ProfileViewModel @Inject constructor(private val repository: TransactionRepository, @ApplicationContext private val context: Context) : ViewModel() {
    private val _installedApp =
        MutableStateFlow<List<AppInfo>>(emptyList<AppInfo>())//修改的,因为是一次性所以使用MutableStateFlow
    val installedApp: StateFlow<List<AppInfo>> = _installedApp  //UI读取

    init {
        showInstalledApps()//相对应的因为使用MutableStateFlow所以手动获取数据
    }

    fun showInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = context.packageManager
            val packages =
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
            //Flags 是一组开关,用来记录APP的所有状态.通常记录是用boolean isSystemAPP True/Flase.
            //Flags是全部值加起来,Example:普通APP(4),允许备份(32768),开启硬件加速(536870912).不需要一大行的Boolean.
            //Flag值 = 4 + 32768 + 536870912 (2^2 + 2^15 + 2^29)
            //Flag Code Example: 1是Flag_SYSTEM 系统APP, 2是 Flag_DEBUGGABLE, 4:是 FLAG_HAS_CODE ...
            val apps = packages //PackageInfo 一个list
                .mapNotNull { it.applicationInfo } //把list转换成ApplicationInfo List, JAVA来的东西都要做默认是Null的处理,把NULL数据丢弃 (Android是JAVA写的)
                .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }//换算成0和1,只要是普通APP最后后一个号码一定是0
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