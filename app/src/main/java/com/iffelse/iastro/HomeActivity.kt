package com.iffelse.iastro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.iffelse.iastro.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val firebaseHelper = FirebaseHelper()

        firebaseHelper.checkIfNameExists(KeyStorePref.getString("userId")!!) { hasName, dataSnapShot ->
            if (hasName) {
                val name = dataSnapShot!!.child("name").getValue(String::class.java)

                setSupportActionBar(binding.toolbar)
                // Change the ActionBar title
                supportActionBar?.title =
                    "Hello ${name!!.split(" ").firstOrNull() ?: ""}"
            }
        }

        // Bottom Navigation Item Selection

        // Load the default fragment (HomeFragment) when the activity starts
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // Set default selected item to Home
        binding.bottomNavigation.selectedItemId = R.id.nav_home

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }

                R.id.nav_trending -> {
                    loadFragment(TrendingFragment())
                    true
                }

                else -> false
            }
        }

    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }


}
