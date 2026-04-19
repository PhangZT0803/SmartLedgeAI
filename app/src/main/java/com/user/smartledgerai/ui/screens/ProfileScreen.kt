package com.user.smartledgerai.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.user.smartledgerai.ui.navigation.ProfileScreenNavigationAction
import com.user.smartledgerai.viewmodel.AuthViewModel
import com.user.smartledgerai.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    onAction: (ProfileScreenNavigationAction) -> Unit
    ){
    val user by authViewModel.user.collectAsState()
    Scaffold ()
    { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)){
            ProfileCard(
                displayName = user?.displayName,
                photoUrl = user?.photoUrl?.toString()
            )
            AppSelectionCard(onAction)
            APIKeyCard()//Future Work
            LogOutButton(authViewModel)
        }
    }
}
@Composable
fun ProfileCard(displayName:String?,photoUrl:String?){
    Card() {
        Row() {
            Spacer(modifier = Modifier.width(8.dp))
            Text("Async Image")
            //Image( upload )
        }
    }
}

@Composable
fun AppSelectionCard(onAction:(ProfileScreenNavigationAction)->Unit){
    Row(){
        Text("Select APP that you want auto record")
        OutlinedButton(
            onClick = { onAction(ProfileScreenNavigationAction.GoToAppSelection) },
            modifier = Modifier.fillMaxWidth()){
            Text("Search APP")
        }
    }
}
@Composable
fun APIKeyCard(){
    Text("API-Key @FutureWork")
}

@Composable
fun LogOutButton(authViewModel: AuthViewModel){
    OutlinedButton(onClick = {authViewModel.signOut()}) { Text("Sign Out")}
}