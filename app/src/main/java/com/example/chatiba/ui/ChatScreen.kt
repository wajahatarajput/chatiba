package com.example.chatiba.ui


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chatiba.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

data class Message(val text: String = "", val sender: String = "", val timestamp: Long = 0L)

@Composable
fun ChatScreen(currentUser: User, recipientUser: User, onSearchUsers: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Message>() }
    val conversationId = getConversationId(currentUser.uid, recipientUser.uid)

    LaunchedEffect(Unit) {
        Firebase.database.reference.child("conversations").child(conversationId).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for (data in snapshot.children) {
                        val message = data.getValue<Message>()
                        if (message != null) {
                            messages.add(message)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
    }


        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(16.dp),
                reverseLayout = true
            ) {
                items(messages) { message ->
                    MessageCard(message)
                }
            }
            Row(modifier = Modifier.padding(16.dp)) {
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Enter message") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val message = Message(text = messageText, sender = currentUser.uid, timestamp = System.currentTimeMillis())
                    Firebase.database.reference.child("conversations").child(conversationId).child("messages").push().setValue(message)
                    messageText = ""
                }) {
                    Text("Send")
                }
            }
        }
    }

@Composable
fun MessageCard(message: Message) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = message.sender, style = MaterialTheme.typography.bodyMedium)
            Text(text = message.text, style = MaterialTheme.typography.bodySmall)
        }
    }
}

fun getConversationId(userId1: String, userId2: String): String {
    return if (userId1 < userId2) {
        "${userId1}_$userId2"
    } else {
        "${userId2}_$userId1"
    }
}