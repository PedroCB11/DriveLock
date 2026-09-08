package com.drivelock.app.notifications

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotificationControlPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val mutableSelectedPackages = MutableStateFlow(readPackages())
    val selectedPackages: StateFlow<Set<String>> = mutableSelectedPackages

    fun toggle(packageName: String) {
        val updated = mutableSelectedPackages.value.toMutableSet().apply {
            if (!add(packageName)) remove(packageName)
        }
        preferences.edit().putStringSet(KEY_PACKAGES, updated).apply()
        mutableSelectedPackages.value = updated
    }

    fun isSelected(packageName: String): Boolean = packageName in mutableSelectedPackages.value

    private fun readPackages(): Set<String> = preferences.getStringSet(KEY_PACKAGES, emptySet()).orEmpty().toSet()

    private companion object {
        const val PREFERENCES_NAME = "drivelock_notification_control"
        const val KEY_PACKAGES = "selected_packages"
    }
}
