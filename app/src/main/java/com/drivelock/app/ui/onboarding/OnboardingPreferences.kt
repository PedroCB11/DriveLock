package com.drivelock.app.ui.onboarding

import android.content.Context

class OnboardingPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun isComplete(): Boolean = preferences.getBoolean(KEY_COMPLETE, false)

    fun markComplete() {
        preferences.edit().putBoolean(KEY_COMPLETE, true).apply()
    }

    fun reset() {
        preferences.edit().putBoolean(KEY_COMPLETE, false).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "drivelock_onboarding"
        const val KEY_COMPLETE = "onboarding_complete"
    }
}
