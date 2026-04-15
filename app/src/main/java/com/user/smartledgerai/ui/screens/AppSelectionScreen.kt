package com.user.smartledgerai.ui.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.user.smartledgerai.viewmodel.AppInfo
import com.user.smartledgerai.viewmodel.ProfileViewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.user.smartledgerai.data.AllowedApp

@Composable
fun AppSelectionScreen(profileViewModel: ProfileViewModel = hiltViewModel()) {
    var searchText by remember { mutableStateOf("") }
    val installedApp = profileViewModel.installedApp.collectAsState()
    val allowedApps = profileViewModel.allowedApps.collectAsState()
    AppSelectionContent(
        installedApps = installedApp.value,
        allowedApps = allowedApps.value,
        searchText,
        onQueryChange = {searchText = it},
        onToggleApp={packageName,appName -> profileViewModel.toggleAllowedApp(packageName,appName)})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionContent(
    installedApps: List<AppInfo>,
    allowedApps: List<AllowedApp>,
    searchText: String,
    onQueryChange: (String) -> Unit,
    onToggleApp:(String,String)->Unit
){
    Scaffold(
            topBar = {
                TopAppBar(
                    title = { SearchBar(searchText, onQueryChange) }
                )
            }
        ) { innerPadding ->
            LazyColumn(contentPadding = innerPadding) {
                items(
                    items = installedApps,
                    key = { it.packageName }
                ) { appInfo ->
                    val isAllowed = allowedApps.any { it.packageName == appInfo.packageName }
                    AppItem(appInfo,isAllowed,onToggle={ onToggleApp(appInfo.packageName,appInfo.appName)})
                }
            }
        }
    }

@Composable
fun SearchBar(query:String,onQueryChange:(String)->Unit){
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {Text("Example: TouchnGo")},
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
    )
}
@Composable
fun AppItem(appInfo: AppInfo,isAllowed:Boolean,onToggle:()->Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)) {
            Image(
                painter = rememberDrawablePainter(drawable = appInfo.icon),
                contentDescription = appInfo.appName,
                modifier = Modifier.size(48.dp)
            )
            Text(text = appInfo.appName)
            Spacer(modifier = Modifier.weight(1f))
            Switch(isAllowed,onCheckedChange = {onToggle()})
        }
    }
}

@Preview
@Composable
fun PreviewAppSelectionScreen(){
    AppSelectionContent (
        installedApps = listOf(
            AppInfo("com.grab","Grab",null),
            AppInfo("com.touchngo","TouchnGo",null)
            ),
        allowedApps = listOf(
            AllowedApp("com.touchngo","TouchnGo")
        ),
        searchText = "",
        onQueryChange = {},
        onToggleApp = {_,_ ->}
    )
}
