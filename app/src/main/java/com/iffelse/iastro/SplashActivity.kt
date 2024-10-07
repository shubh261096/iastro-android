package com.iffelse.iastro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.animation.AnimationUtils
import android.widget.ImageView

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        KeyStorePref.initialize(applicationContext)
        // Find the rotating outer image
        val rotatingImage = findViewById<ImageView>(R.id.iv_rotate)

        // Load rotate animation from XML and apply to the rotating image
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        rotatingImage.startAnimation(rotateAnimation)

        // Bouncing logo
        val logo = findViewById<ImageView>(R.id.iv_logo)
        val bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        logo.startAnimation(bounceAnimation)

        if (KeyStorePref.getBoolean("isLogin"))
        // Stay on the splash screen for 3 seconds before transitioning to the next screen
            if (!TextUtils.isEmpty(KeyStorePref.getString("name"))) {
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 0) // 3 seconds
            } else {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                finish()
            }
        else {
            // Stay on the splash screen for 3 seconds before transitioning to the next screen
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }, 0) // 3 seconds
        }
    }
}