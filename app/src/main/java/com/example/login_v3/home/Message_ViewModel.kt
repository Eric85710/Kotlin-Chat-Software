// Message_ViewModel.kt
package com.example.login_v3.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.login_v3.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


data class Contact(
    val name: String,
    val avatarResId: Int // 或者用 String 存圖片 URL
)

class MessageViewModel : ViewModel() {

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts

    init {
        viewModelScope.launch {
            _contacts.value = listOf(
                Contact("Alice", R.drawable.avatar_v1),
                Contact("Bob", R.drawable.avatar_v1),
                Contact("Charlie", R.drawable.avatar_v1),
                Contact("David", R.drawable.avatar_v1),
                Contact("nigga", R.drawable.avatar_v1),
                Contact("gbl", R.drawable.avatar_v1),
                Contact("威威夢夢", R.drawable.avatar_v1)
            )
        }
    }
}

