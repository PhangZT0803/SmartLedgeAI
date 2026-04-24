package com.user.smartledgerai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.user.smartledgerai.ui.navigation.ProfileScreenNavigationAction
import com.user.smartledgerai.viewmodel.AuthViewModel
import com.user.smartledgerai.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    onAction: (ProfileScreenNavigationAction) -> Unit
) {
    val user by authViewModel.user.collectAsState()
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.windowInsetsPadding(WindowInsets.statusBars))

            // User Profile Header
            ProfileHeader(
                displayName = user?.displayName ?: "User",
                email = user?.email ?: "No email linked",
                photoUrl = user?.photoUrl?.toString()
            )

            Spacer(Modifier.height(32.dp))

            // Settings Sections
            Text(
                text = "GENERAL SETTINGS",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
            )

            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = colors.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Default.NotificationsActive,
                        title = "Auto-Record Apps",
                        subtitle = "Select apps to track notifications",
                        onClick = { onAction(ProfileScreenNavigationAction.GoToAppSelection) }
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = colors.outlineVariant)
                    ProfileMenuItem(
                        icon = Icons.Default.VpnKey,
                        title = "Gemini API Key",
                        subtitle = "Configure your AI engine",
                        onClick = { /* Future Work */ }
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.Category,
                        title = "Transaction Categories",
                        subtitle = "Manage income & spending labels",
                        onClick = {
                            onAction(ProfileScreenNavigationAction.GoToCategories)
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            // 4. LogOut
            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = colors.surface)
            ) {
                ProfileMenuItem(
                    icon = Icons.Default.Logout,
                    title = "Sign Out",
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = {
                        authViewModel.signOut()
                    },
                    showChevron = false
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(displayName: String, email: String, photoUrl: String?) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile image container with gradient border
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    brush = Brush.linearGradient(listOf(colors.primary, colors.secondary)),
                    shape = CircleShape
                )
                .padding(3.dp)
        ) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(colors.surface),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.width(20.dp))

        Column {
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
    showChevron: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon background
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(titleColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = titleColor, modifier = Modifier.size(20.dp))
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = titleColor)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (showChevron) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}