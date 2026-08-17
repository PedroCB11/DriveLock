package com.drivelock.app

import android.app.Application

class DriveLockApplication : Application() {
    val container by lazy { AppContainer(this) }
}
