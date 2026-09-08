package com.drivelock.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.drivelock.app.navigation.DriveLockNavHost
import com.drivelock.app.ui.theme.DriveLockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as DriveLockApplication).container
        setContent {
            val themeMode by container.settingsPreferences.themeMode.collectAsStateWithLifecycle()
            DriveLockTheme(themeMode) { DriveLockNavHost(rememberNavController(), container) }
        }
    }
}
