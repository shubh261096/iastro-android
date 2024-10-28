package com.iffelse.iastro

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.iffelse.iastro.utils.KeyStorePref
import com.razorpay.Checkout

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        KeyStorePref.initialize(this)

        FirebaseInAppMessaging.getInstance().isAutomaticDataCollectionEnabled = true
        if (KeyStorePref.getBoolean("isLogin")) {
            Firebase.crashlytics.setUserId(KeyStorePref.getString("userId")!!)
        }

        /**
         * Preload payment resources
         */
        Checkout.preload(this)
    }
}
