package com.user.smartledgerai.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.user.smartledgerai.ui.navigation.ProfileScreenNavigationAction
import com.user.smartledgerai.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onAction: (ProfileScreenNavigationAction) -> Unit
    ){
    Scaffold ()
    { innerPadding ->
        Column(){
            Row(){
                Text("Select APP that you want auto record")
                OutlinedButton(
                    onClick = { onAction(ProfileScreenNavigationAction.GoToAppSelection) },
                    modifier = Modifier.fillMaxWidth()){
                    Text("Search APP")
                }
            }
        }
    }
}