package com.example.chatiba

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.chatiba.ui.AuthScreen
import com.example.chatiba.ui.ChatScreen
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.chatiba.ui.UserSearchScreen

data class User(val email: String = "", val uid: String = "")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentUser = remember { FirebaseAuth.getInstance().currentUser }
            var isAuthenticated by remember { mutableStateOf(currentUser != null) }
            var showUserSearch by remember { mutableStateOf(false) }
            var selectedUser by remember { mutableStateOf<User?>(null) }

            if (isAuthenticated) {
                if (showUserSearch) {
                    UserSearchScreen(onUserSelected = { user ->
                        selectedUser = user
                        showUserSearch = false
                    })
                } else {
                    selectedUser?.let { recipient ->
                        ChatScreen(
                            currentUser = User(
                                email = currentUser?.email ?: "",
                                uid = currentUser?.uid ?: ""
                            ),
                            recipientUser = recipient,
                            onSearchUsers = { showUserSearch = true }
                        )
                    } ?: run {
                        UserSearchScreen(onUserSelected = { user ->
                            selectedUser = user
                        })
                    }
                }
            } else {
                AuthScreen(onAuthSuccess = { isAuthenticated = true })
            }
        }
    }
}