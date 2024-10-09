package com.iffelse.iastro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.iffelse.iastro.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity(), HomeFragment.OnCardClickListener {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setSupportActionBar(binding.toolbar)
//        supportActionBar?.title =
//            "Chat with Astrologer"

//        val firebaseHelper = FirebaseHelper()
//
//        firebaseHelper.checkIfNameExists(KeyStorePref.getString("userId")!!) { hasName, dataSnapShot ->
//            if (hasName) {
//                val name = dataSnapShot!!.child("name").getValue(String::class.java)
//
//                setSupportActionBar(binding.toolbar)
//                // Change the ActionBar title
//                supportActionBar?.title =
//                    "Hello ${name!!.split(" ").firstOrNull() ?: ""}"
//            }
//        }

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
                    binding.toolbarTitle.text = "iastro"
                    binding.toolbarImage.visibility = View.VISIBLE
                    // Load rotate animation from XML and apply to the rotating image
                    val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_logo)
                    binding.toolbarImage.startAnimation(rotateAnimation)
                    loadFragment(HomeFragment())
                    true
                }

                R.id.nav_trending -> {
                    binding.toolbarTitle.text = "Trending"
                    binding.toolbarImage.clearAnimation()
                    binding.toolbarImage.visibility = View.GONE
                    loadFragment(TrendingFragment())
                    true
                }

                R.id.nav_call -> {
                    binding.toolbarTitle.text = "Call with Astrologer"
                    binding.toolbarImage.clearAnimation()
                    binding.toolbarImage.visibility = View.GONE
                    loadFragment(CallFragment())
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

    override fun onCardClick(page: String) {
        if (page == "trending")
            binding.bottomNavigation.selectedItemId = R.id.nav_trending
        else if (page == "call")
            binding.bottomNavigation.selectedItemId = R.id.nav_call
    }

}
