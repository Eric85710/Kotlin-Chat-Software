package com.example.login_v3.home

import androidx.lifecycle.ViewModel
import com.example.login_v3.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Server(
    val id: String,
    val name: String,
    val server_icon: Int
)

class ServerViewModel : ViewModel() {
    private val _serverList = MutableStateFlow<List<Server>>(emptyList())
    val serverList: StateFlow<List<Server>> = _serverList

    private val _currentServer = MutableStateFlow<Server?>(null)
    val currentServer: StateFlow<Server?> = _currentServer

    init {
        // 測試用：初始化一些伺服器
        _serverList.value = listOf(
            Server("1", "伺服器A", R.drawable.avatar_v1),
            Server("2", "伺服器B", R.drawable.avatar_v1),
            Server("3", "伺服器C", R.drawable.avatar_v1)
        )
    }

    fun addServer(server: Server) {
        _serverList.value = _serverList.value + server
    }

    fun selectServer(serverId: String) {
        _currentServer.value = _serverList.value.find { it.id == serverId }
    }

    fun removeServer(serverId: String) {
        _serverList.value = _serverList.value.filterNot { it.id == serverId }
        if (_currentServer.value?.id == serverId) {
            _currentServer.value = null
        }
    }
}
