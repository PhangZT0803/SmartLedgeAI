package com.user.smartledgerai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel@Inject constructor(): ViewModel() {
    val user = MutableStateFlow<FirebaseUser?>(null)//因为null是正常的,也不需要显示数据所以不需要init,MutableStateFlow就是当获取到FirebaseUser的时候通知UI变化

    init {
        // App 打开时检查是否已登录
        user.value = FirebaseAuth.getInstance().currentUser
    }

    //SignIn
    fun onGoogleSignInResult(idToken:String){
        //FirebaseUser
        //firebaseUser?.uid        // 唯一 ID（数据库里用来识别用户的 key）
        //firebaseUser?.email      // 邮箱
        //firebaseUser?.displayName
        //firebaseUser?.photoUrl
        //Nullable是因为Google Authentication流程中间可能会fail
        viewModelScope.launch {
            try {
                Timber.d("Start Firebase Authentication")
                val credential = GoogleAuthProvider.getCredential(idToken, null)

                val result = FirebaseAuth.getInstance().signInWithCredential(credential).await()
                Timber.d("Login Successful: ${result.user?.email}")
                user.value = result.user
            } catch (e: Exception) {
                Timber.e( "Login Failed: ${e.message}")
            }
        }
    }


    fun signOut() {
        FirebaseAuth.getInstance().signOut()//主动清除本地储存的token
        user.value = null
    }

    //FirebaseSDK登录机制
    //第一次登录 → Firebase SDK 自动存 token 到本地 → 不需要管
    //重开 App   → FirebaseAuth.getInstance().currentUser 自动读本地 token
    //→ token 没过期 → 直接返回 FirebaseUser
    //→ token 过期了 → SDK 自动用 refresh token 刷新
}