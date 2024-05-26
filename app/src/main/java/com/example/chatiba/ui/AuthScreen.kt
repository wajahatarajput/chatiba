package com.example.chatiba.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.chatiba.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                val auth = Firebase.auth
                if (isRegistering) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = task.result?.user
                                user?.let {
                                    val userRef = Firebase.database.reference.child("users").child(user.uid)
                                    userRef.setValue(User(email = user.email ?: "", uid = user.uid))
                                }
                                onAuthSuccess()
                            } else {
                                errorMessage = task.exception?.message
                            }
                        }
                } else {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onAuthSuccess()
                            } else {
                                errorMessage = task.exception?.message
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRegistering) "Register" else "Login")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { isRegistering = !isRegistering }) {
            Text(if (isRegistering) "Already have an account? Login" else "Don't have an account? Register")
        }
        Spacer(modifier = Modifier.height(8.dp))
        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}
