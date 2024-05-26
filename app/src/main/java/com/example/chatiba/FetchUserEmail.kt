package com.example.chatiba

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

fun fetchUserEmail(uid: String, callback: (String?) -> Unit) {
    Firebase.database.reference.child("users").child(uid).get().addOnSuccessListener {
        val user = it.getValue(User::class.java)
        callback(user?.email)
    }.addOnFailureListener {
        callback(null)
    }
}
