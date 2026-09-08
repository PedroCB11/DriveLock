package com.drivelock.app.ui.settings

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.drivelock.app.notifications.NotificationControlPreferences
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn

data class NotificationAppsUiState(
    val query: String = "",
    val apps: List<SelectableApp> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val accessEnabled: Boolean = false,
)

@OptIn(FlowPreview::class)
class NotificationAppsViewModel(
    private val context: Context,
    private val preferences: NotificationControlPreferences,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val installedApps = context.launchableApps()
    private val accessEnabled = MutableStateFlow(hasAccess())

    val uiState: StateFlow<NotificationAppsUiState> = combine(
        query.debounce(300),
        preferences.selectedPackages,
        accessEnabled,
    ) { search, selected, access ->
        NotificationAppsUiState(
            query = search,
            apps = installedApps.filter { it.label.contains(search, ignoreCase = true) || it.packageName.contains(search, ignoreCase = true) },
            selectedPackages = selected,
            accessEnabled = access,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        NotificationAppsUiState(apps = installedApps, selectedPackages = preferences.selectedPackages.value, accessEnabled = hasAccess()),
    )

    fun updateQuery(value: String) { query.value = value }
    fun toggle(packageName: String) = preferences.toggle(packageName)
    fun refreshPermission() { accessEnabled.value = hasAccess() }

    private fun hasAccess() = context.packageName in NotificationManagerCompat.getEnabledListenerPackages(context)

    class Factory(
        private val context: Context,
        private val preferences: NotificationControlPreferences,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NotificationAppsViewModel(context.applicationContext, preferences) as T
    }
}

private fun Context.launchableApps(): List<SelectableApp> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return packageManager.queryIntentActivities(intent, 0)
        .map { info -> SelectableApp(info.activityInfo.packageName, info.loadLabel(packageManager).toString()) }
        .filter { it.packageName != packageName }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase() }
}
