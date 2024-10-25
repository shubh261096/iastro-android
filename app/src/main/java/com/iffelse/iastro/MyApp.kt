package com.iffelse.iastro

import android.app.Application
import com.cashfree.pg.api.CFPaymentGatewayService
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.iffelse.iastro.utils.KeyStorePref

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        KeyStorePref.initialize(this)

        FirebaseInAppMessaging.getInstance().isAutomaticDataCollectionEnabled = true
        if (KeyStorePref.getBoolean("isLogin")) {
            Firebase.crashlytics.setUserId(KeyStorePref.getString("userId")!!)
        }
        CFPaymentGatewayService.initialize(this); // Application Context.
    }
}
