package com.iffelse.iastro
import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.inappmessaging.FirebaseInAppMessaging

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        KeyStorePref.initialize(this)

        FirebaseDatabase.getInstance().setPersistenceEnabled(false)
        FirebaseInAppMessaging.getInstance().isAutomaticDataCollectionEnabled = true
    }
}
