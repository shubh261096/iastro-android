package com.iffelse.iastro

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.iffelse.iastro.utils.AppConstants
import com.iffelse.iastro.utils.KeyStorePref
import com.razorpay.Checkout
import com.sceyt.chatuikit.SceytChatUIKit
import com.sceyt.chatuikit.styles.StyleCustomizer
import com.sceyt.chatuikit.styles.input.MessageInputStyle
import java.util.UUID

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

        if (!KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID).isNullOrEmpty())
            SceytChatUIKit.initialize(
                this,
                apiUrl = "https://us-ohio-api.sceyt.com",
                appId = "y2k4jg28xy",
                clientId = KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!,
                enableDatabase = true
            )
        else
            SceytChatUIKit.initialize(
                this,
                apiUrl = "https://us-ohio-api.sceyt.com",
                appId = "y2k4jg28xy",
                clientId = UUID.randomUUID().toString(),
                enableDatabase = true
            )

        MessageInputStyle.styleCustomizer = StyleCustomizer { context, style ->
            style.copy(
                enableSendAttachment = false,
                enableVoiceRecord = false
            )
        }

        SceytChatUIKit.theme.colors = SceytChatUIKit.theme.colors.copy(
            accentColor = R.color.orange
        )
    }
}
