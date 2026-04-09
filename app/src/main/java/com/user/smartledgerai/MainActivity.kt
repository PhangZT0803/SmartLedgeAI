package com.user.smartledgerai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.user.smartledgerai.ui.navigation.MainNavigation
import com.user.smartledgerai.ui.theme.SmartLedgerAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartLedgerAITheme {
                Surface {
                    MainNavigation()
                }
            }
        }
    }
}