package com.example.chatiba.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
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

@Composable
fun UserSearchScreen(onUserSelected: (User) -> Unit) {
    var query by remember { mutableStateOf("") }
    val users = remember { mutableStateListOf<User>() }

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
                val ref = Firebase.database.reference.child("users")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        users.clear()
                        for (data in snapshot.children) {
                            val user = data.getValue<User>()
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
