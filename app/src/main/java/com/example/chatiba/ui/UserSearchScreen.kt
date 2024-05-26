package com.example.chatiba.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chatiba.Conversations
import com.example.chatiba.Message
import com.example.chatiba.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun UserSearchScreen(
    currentUser: User,
    onUserSelected: (User) -> Unit,
    onConversationSelected: (Conversations) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val users = remember { mutableStateListOf<User>() }
    val conversations = remember { mutableStateListOf<Conversations>() }
    val messages = remember { mutableStateMapOf<String, List<Message>>() } // Map to store messages for each conversation

    // Fetch all conversations for the current user
    LaunchedEffect(currentUser.uid) {
        val conversationsRef = Firebase.database.reference.child("conversations")
        conversationsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                conversations.clear()
                for (data in snapshot.children) {
                    val conversation = data.getValue(Conversations::class.java)
                    if (conversation != null && conversation.members.contains(currentUser.uid)) {
                        conversations.add(conversation)
                        // Fetch messages for each conversation
                        val messagesRef = Firebase.database.reference.child("messages").child(data.key!!)
                        messagesRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(messageSnapshot: DataSnapshot) {
                                val messageList = mutableListOf<Message>()
                                for (messageData in messageSnapshot.children) {
                                    val message = messageData.getValue(Message::class.java)
                                    message?.let { messageList.add(it) }
                                }
                                messages[data.key!!] = messageList
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle database error
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    // Display users and conversations
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search users") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                val usersRef = Firebase.database.reference.child("users")
                usersRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        users.clear()
                        for (data in snapshot.children) {
                            val user = data.getValue(User::class.java)
                            if (user != null && user.email.contains(query, ignoreCase = true)) {
                                users.add(user)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle database error
                    }
                })
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text("Conversations", style = MaterialTheme.typography.titleLarge)
        LazyColumn {
            items(conversations) { conversation ->
                ConversationItem(
                    conversation = conversation,
                    messages = messages[conversation.id],
                    onClick = { onConversationSelected(conversation) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Users", style = MaterialTheme.typography.titleLarge)
        LazyColumn {
            items(users) { user ->
                UserItem(user = user, onClick = { onUserSelected(user) })
            }
        }
    }
}

@Composable
fun UserItem(user: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = user.email, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ConversationItem(conversation: Conversations, messages: List<Message>?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Conversation ID: ${conversation.id}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
