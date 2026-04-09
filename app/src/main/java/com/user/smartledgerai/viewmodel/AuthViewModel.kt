package com.user.smartledgerai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.user.smartledgerai.usecase.GoogleSignInUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AuthViewModel: ViewModel() {
    val user = MutableStateFlow<FirebaseUser?>(null)

    fun onGoogleSignInResult(idToken:String){

        val signInUseCase = GoogleSignInUseCase()
        viewModelScope.launch {
            user.value = signInUseCase.execute(idToken)
        }
    }
}