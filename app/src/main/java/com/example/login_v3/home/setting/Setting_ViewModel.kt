package com.example.login_v3.home.setting


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SettingItem(
    val id: String,
    val title: String,
    val description: String? = null,
    val setting_icon: ImageVector,
    val iconKey: String
)



class SettingViewModel : ViewModel() {

    private val _settings = MutableStateFlow<List<SettingItem>>(emptyList())
    val settings: StateFlow<List<SettingItem>> = _settings

    init {
        _settings.value = listOf(
            SettingItem("profile", "Profile", "Manage your account", Icons.Filled.Person, "profile"),
            SettingItem("theme", "Theme", "Customize app appearance", Icons.Filled.Palette, "theme"),
            SettingItem("devices", "Devices", "Manage connected devices", Icons.Filled.Devices, "devices"),
            SettingItem("subscription", "Subscription", "View or update your plan", Icons.Filled.Subscriptions, "subscribetion")
        )
    }
}

