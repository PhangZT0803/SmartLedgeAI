package com.user.smartledgerai.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.user.smartledgerai.R
import com.user.smartledgerai.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun OnBoarding(authViewModel:AuthViewModel = viewModel()) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val user by authViewModel.user.collectAsState()

    LaunchedEffect(user) {
        if (user != null) {
            // TODO: 跳转主页
        }
    }

    val credentialManager = remember { CredentialManager.create(context)}
    val googleIdOption = remember{
        GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()
    }
    val scope = rememberCoroutineScope()
    Scaffold()
    { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp)
            .fillMaxSize())
        {
            Text("SmartLedgerAI")

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                    onClick = { scope.launch {
                        try {
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            val result = credentialManager.getCredential(
                                request = request,
                                context = context
                            )

                            val credential = result.credential
                            if (credential is GoogleIdTokenCredential) {
                                authViewModel.onGoogleSignInResult(credential.idToken)
                            }
                        } catch (e: Exception) {
                            Timber.e("Sign-In failed: ${e.message}")
                        }
                    }
                              },
                    modifier = Modifier.fillMaxWidth()
            ) {
                //Login with google
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google_logo_rounded),
                        contentDescription = "Google",
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Google")
                }
            }
                Spacer(modifier = Modifier.height(8.dp))

                Text("OR")

                Spacer(modifier = Modifier.height(8.dp))


                    OutlinedTextField(
                        value = username,
                        onValueChange = { newUsername: String ->
                            username = newUsername
                        },
                        label = { Text("Username") },
                        placeholder = { Text("Username") },
                        shape = RoundedCornerShape(52),
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { newPassword: String ->
                            password = newPassword
                        },
                        label = { Text("Password") },
                        placeholder = { Text("Password") },
                        shape = RoundedCornerShape(52),
                        modifier = Modifier.fillMaxWidth()
                    )

            }

        }



    }
@Preview
@Composable
fun PreviewOnBoading(){
    OnBoarding()
}