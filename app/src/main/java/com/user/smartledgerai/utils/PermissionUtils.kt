package com.user.smartledgerai.utils

import android.content.Context
import android.provider.Settings
import android.util.Log

object PermissionUtils {
    fun isNotificationListenerEnable(context: Context): Boolean {
        val flat:String? = Settings.Secure.getString(
            context.contentResolver, //context理解为studentID,Resolver理解为特别ID卡,基本上只有跨APP去读取别的数据才会使用到,比如现在就是跨APP去读取Setting的数据
            "enabled_notification_listeners" //选择读取Setting的数据
        )
        return flat?.contains(context.packageName) ?: false//查找Setting里面的Service注册表有没有我们的package名字
    }
}
