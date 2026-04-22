package com.user.smartledgerai.ui.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
    val installedApps by profileViewModel.installedApp.collectAsState()
    val allowedApps by profileViewModel.allowedApps.collectAsState()

    val filteredApps = remember (searchText, installedApps) {
        if (searchText.isBlank()) installedApps
        else installedApps.filter { it.appName.contains(searchText, ignoreCase = true) }
    }

    AppSelectionContent(
        installedApps = filteredApps,
        allowedApps = allowedApps,
        searchText,
        onQueryChange = {searchText = it},
        onToggleApp={packageName,appName ->
            profileViewModel.toggleAllowedApp(packageName,appName)
        }
    )
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
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Auto-Record Apps",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))
                        // Search Bar
                        SearchBar(searchText, onQueryChange)
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = installedApps,
                    key = { it.packageName }
                ) { appInfo ->
                    val isAllowed = allowedApps.any { it.packageName == appInfo.packageName }

                    AppListItem(
                        appInfo = appInfo,
                        isAllowed = isAllowed,
                        onToggle = { onToggleApp(appInfo.packageName, appInfo.appName) }
                    )

                    // Refined divider design
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 72.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit){
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(2.dp, RoundedCornerShape(28.dp)),
        placeholder = { Text("Search system apps...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(28.dp),
        singleLine = true
    )
}
@Composable
fun AppListItem(appInfo: AppInfo,isAllowed:Boolean,onToggle:()->Unit) {
    ListItem(
        headlineContent = {
            Text(
                appInfo.appName,
                style = MaterialTheme.typography.titleMedium,
                color = if (isAllowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = { Text(appInfo.packageName, style = MaterialTheme.typography.bodySmall) },
        leadingContent = {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp
            ) {
                Image(
                    painter = rememberDrawablePainter(drawable = appInfo.icon),
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp).clip(RoundedCornerShape(8.dp))
                )
            }
        },
        trailingContent = {
            Switch(
                checked = isAllowed,
                onCheckedChange = { onToggle() },
                thumbContent = if (isAllowed) {
                    { Icon(Icons.Default.Search, null, Modifier.size(SwitchDefaults.IconSize)) }
                } else null
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
