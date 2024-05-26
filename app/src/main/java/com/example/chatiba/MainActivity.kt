package com.example.chatiba

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.chatiba.ui.AuthScreen
import com.example.chatiba.ui.ChatScreen
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.chatiba.ui.UserSearchScreen

data class User(val email: String = "", val uid: String = "")
data class Conversations(val id: String, val members: List<String>)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentUser = remember { FirebaseAuth.getInstance().currentUser }
            var isAuthenticated by remember { mutableStateOf(currentUser != null) }
            var selectedUser by remember { mutableStateOf<User?>(null) }
            var selectedConversation by remember { mutableStateOf<Conversations?>(null) }
            var recipientUser by remember { mutableStateOf<User?>(null) }

            if (isAuthenticated) {
                if (selectedUser == null && selectedConversation == null) {
                    UserSearchScreen(
                        currentUser = User(email = currentUser?.email ?: "", uid = currentUser?.uid ?: ""),
                        onUserSelected = { user ->
                            selectedUser = user
                        },
                        onConversationSelected = { conversation ->
                            selectedConversation = conversation
                        }
                    )
                } else if (selectedUser != null) {
                    ChatScreen(
                        currentUser = User(
                            email = currentUser?.email ?: "",
                            uid = currentUser?.uid ?: ""
                        ),
                        recipientUser = selectedUser!!,
                        onSearchUsers = {
                            selectedUser = null
                            selectedConversation = null
                        }
                    )
                } else if (selectedConversation != null) {
                    val recipientUid = selectedConversation!!.members.first { it != currentUser?.uid }
                    LaunchedEffect(recipientUid) {
                        fetchUserEmail(recipientUid) { email ->
                            if (email != null) {
                                recipientUser = User(email = email, uid = recipientUid)
                            }
                        }
                    }

                    recipientUser?.let { user ->
                        ChatScreen(
                            currentUser = User(
                                email = currentUser?.email ?: "",
                                uid = currentUser?.uid ?: ""
                            ),
                            recipientUser = user,
                            onSearchUsers = {
                                selectedUser = null
                                selectedConversation = null
                                recipientUser = null
                            }
                        )
                    }
                }
            } else {
                AuthScreen(onAuthSuccess = { isAuthenticated = true })
            }
        }
    }
}