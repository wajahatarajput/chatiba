package com.example.chatiba.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chatiba.Message
import com.example.chatiba.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

@Composable
fun ChatScreen(
    currentUser: User,
    recipientUser: User,
    onBack: () -> Unit
) {
    var messages by remember { mutableStateOf(listOf<Message>()) } // Renamed to avoid conflict
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(currentUser.uid, recipientUser.uid) {
        val ref = Firebase.database.reference.child("messages").child(currentUser.uid).child(recipientUser.uid)
        val recipientRef = Firebase.database.reference.child("messages").child(recipientUser.uid).child(currentUser.uid)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newMessages = mutableListOf<Message>()
                for (data in snapshot.children) {
                    val message = data.getValue<Message>()
                    if (message != null) {
                        newMessages.add(message)
                    }
                }
                messages = newMessages.sortedBy { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        }

        ref.addValueEventListener(listener)
        recipientRef.addValueEventListener(listener)


    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack) {
                Text("Back")
            }
            Text(text = "Chat with ${recipientUser.email}", style = MaterialTheme.typography.titleLarge)
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { message ->
                Text(text = message.text, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                if (messageText.isNotBlank()) {
                    val ref = Firebase.database.reference.child("messages")
                    val senderRef = ref.child(currentUser.uid).child(recipientUser.uid)
                    val recipientRef = ref.child(recipientUser.uid).child(currentUser.uid)
                    val message = Message(senderId = currentUser.uid, text = messageText, timestamp = System.currentTimeMillis())
                    senderRef.push().setValue(message)
                    recipientRef.push().setValue(message)
                    messageText = ""
                }
            }) {
                Text("Send")
            }
        }
    }
}

