package com.user.smartledgerai.usecase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleSignInUseCase {
    //FirebaseUser
    //firebaseUser?.uid        // 唯一 ID（数据库里用来识别用户的 key）
    //firebaseUser?.email      // 邮箱
    //firebaseUser?.displayName
    //firebaseUser?.photoUrl
    //Nullable是因为Google Authentication流程中间可能会fail
    suspend fun execute(idToken: String): FirebaseUser? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = FirebaseAuth.getInstance().signInWithCredential(credential).await()
        return result.user
    }
}