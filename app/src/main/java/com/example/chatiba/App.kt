package com.example.chatiba

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.database.database

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Firebase.database.setPersistenceEnabled(true)
        Firebase.database.reference.database.setPersistenceEnabled(true)
        Firebase.database.reference.database.getReferenceFromUrl("https://chatapp-da272-default-rtdb.firebaseio.com")
    }
}
