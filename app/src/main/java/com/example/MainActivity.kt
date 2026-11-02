package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.ui.StreakViewModel
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.StreakTheme

import android.content.Intent
import android.net.Uri

class MainActivity : ComponentActivity() {

    private val viewModel: StreakViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        handleDeepLinkIntent(intent)

        setContent {
            val isDarkTheme by viewModel.isDarkMode.collectAsState()
            val themeAccent by viewModel.currentThemeAccent.collectAsState()

            StreakTheme(darkTheme = isDarkTheme, accentTheme = themeAccent) {
                AppNavigation(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = { viewModel.toggleDarkMode(it) }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        val data: Uri? = intent?.data
        if (data != null) {
            val linkString = data.toString()
            if (linkString.contains("join")) {
                viewModel.joinCircleFromInviteLinkOrCode(linkString)
            }
        }
    }
}

