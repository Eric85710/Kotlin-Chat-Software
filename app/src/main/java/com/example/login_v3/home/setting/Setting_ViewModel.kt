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
    val iconKey: String,
    val keywords: List<String> = emptyList()
)



class SettingViewModel : ViewModel() {

    private val _settings = MutableStateFlow<List<SettingItem>>(emptyList())
    val settings: StateFlow<List<SettingItem>> = _settings

    init {
        _settings.value = listOf(
            SettingItem("profile", "Profile", "Manage your account", Icons.Filled.Person, "profile", listOf("個人資料", "頭像", "名稱")),
            SettingItem("theme", "Theme", "Customize app appearance", Icons.Filled.Palette, "theme", listOf("dark", "light", "wallpaper", "mode", "主題", "深色", "淺色", "背景")),
            SettingItem("devices", "Devices", "Manage connected devices", Icons.Filled.Devices, "devices", listOf("裝置", "登入紀錄")),
            SettingItem("subscription", "Subscription", "View or update your plan", Icons.Filled.Subscriptions, "subscribetion", listOf("訂閱", "方案"))
        )
    }
}

