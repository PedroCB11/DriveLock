package com.drivelock.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.drivelock.app.navigation.DriveLockNavHost
import com.drivelock.app.ui.theme.DriveLockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as DriveLockApplication).container
        setContent {
            DriveLockTheme { DriveLockNavHost(rememberNavController(), container) }
        }
    }
}

