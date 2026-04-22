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
class AuthViewModel @Inject constructor() : ViewModel() {
    val user = MutableStateFlow<FirebaseUser?>(null)

    init {
        user.value = FirebaseAuth.getInstance().currentUser
    }

    fun onGoogleSignInResult(idToken: String) {
        viewModelScope.launch {
            try {
                Timber.d("Starting Firebase Authentication")
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = FirebaseAuth.getInstance().signInWithCredential(credential).await()
                Timber.d("Authentication successful for: ${result.user?.email}")
                user.value = result.user
            } catch (e: Exception) {
                Timber.e("Authentication failed: ${e.message}")
            }
        }
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        user.value = null
    }
}