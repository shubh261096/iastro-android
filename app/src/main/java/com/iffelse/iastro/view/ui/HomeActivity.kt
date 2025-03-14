package com.iffelse.iastro.view.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.iffelse.iastro.BuildConfig
import com.iffelse.iastro.R
import com.iffelse.iastro.databinding.ActivityHomeBinding
import com.iffelse.iastro.model.BaseErrorModel
import com.iffelse.iastro.model.response.ChatTokenResponseModel
import com.iffelse.iastro.model.response.CommonResponseModel
import com.iffelse.iastro.utils.AppConstants
import com.iffelse.iastro.utils.KeyStorePref
import com.iffelse.iastro.utils.OkHttpNetworkProvider
import com.iffelse.iastro.utils.Utils
import com.iffelse.iastro.view.fragment.CallFragment
import com.iffelse.iastro.view.fragment.ChatListFragment
import com.iffelse.iastro.view.fragment.HomeFragment
import com.iffelse.iastro.view.fragment.TrendingFragment
import com.sceyt.chatuikit.SceytChatUIKit
import com.sceyt.chatuikit.data.managers.connection.ConnectionEventManager
import com.sceyt.chatuikit.data.models.SceytResponse
import com.sceyt.chatuikit.data.models.channels.CreateChannelData
import com.sceyt.chatuikit.data.models.channels.SceytMember
import com.sceyt.chatuikit.data.models.messages.SceytUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class HomeActivity : BaseActivity(), HomeFragment.OnCardClickListener {

    private lateinit var binding: ActivityHomeBinding

    companion object {
        private const val TAG = "HomeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the click listener for the custom button
        binding.toggle.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }


        if (KeyStorePref.getBoolean(AppConstants.KEY_STORE_IS_LOGIN)) {
            if (KeyStorePref.getString(AppConstants.KEY_STORE_NAME).isNullOrEmpty()) {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            // Stay on the splash screen for 3 seconds before transitioning to the next screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        // Load the default fragment (HomeFragment) when the activity starts
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // Set default selected item to Home
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        // Load rotate animation from XML and apply to the rotating image
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_logo)
        binding.toolbarImage.startAnimation(rotateAnimation)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    binding.toolbarTitle.text = resources.getText(R.string.app_name)
                    binding.toolbarImage.visibility = View.VISIBLE
                    // Load rotate animation from XML and apply to the rotating image
                    binding.toolbarImage.startAnimation(
                        AnimationUtils.loadAnimation(
                            this,
                            R.anim.rotate_logo
                        )
                    )
                    loadFragment(HomeFragment())
                    true
                }

                R.id.nav_trending -> {
                    binding.toolbarTitle.text = resources.getText(R.string.trending)
                    binding.toolbarImage.clearAnimation()
                    binding.toolbarImage.visibility = View.GONE
                    loadFragment(TrendingFragment())
                    true
                }

                R.id.chat -> {
                    binding.toolbarTitle.text = resources.getText(R.string.chat)
                    binding.toolbarImage.clearAnimation()
                    binding.toolbarImage.visibility = View.GONE
                    loadFragment(ChatListFragment())
                    true
                }

                R.id.nav_call -> {
                    binding.toolbarTitle.text = resources.getText(R.string.call_with_astrologer)
                    binding.toolbarImage.clearAnimation()
                    binding.toolbarImage.visibility = View.GONE
                    loadFragment(CallFragment())
                    true
                }

                else -> false
            }
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finishAffinity()
                }
            }
        })

        subscribeToZodiacSignTopic()
        updateFcmTokenApi()
        updateDrawerUI()
        getChatToken()
    }

    private fun getChatToken() {
        lifecycleScope.launch(Dispatchers.IO) {
            val headers = mutableMapOf<String, String>()
            headers["Content-Type"] = "application/x-www-form-urlencoded"
            headers["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)

            val jsonObjectBody = JSONObject()
            jsonObjectBody.put("phone", KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID))

            OkHttpNetworkProvider.post(
                BuildConfig.BASE_URL + "common/chat",
                jsonObjectBody,
                headers,
                null,
                null,
                ChatTokenResponseModel::class.java,
                object : OkHttpNetworkProvider.NetworkListener<ChatTokenResponseModel> {
                    override fun onResponse(response: ChatTokenResponseModel?) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Utils.hideProgress()
                            if (response != null) {
                                Log.i(TAG, "onResponse: $response")
                                if (response.error == false && !response.chatToken.isNullOrEmpty()) {
                                    connectToChatClient(response.chatToken)
                                } else {
                                    Toast.makeText(
                                        this@HomeActivity,
                                        response.message ?: "Something went wrong!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                        lifecycleScope.launch(Dispatchers.Main) {
                            Utils.hideProgress()
                            Toast.makeText(
                                this@HomeActivity,
                                error?.message ?: "Something went wrong!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                })
        }
    }
    private suspend fun connectToChatClient(token: String) {
        // Step 1: Connect to the chat client
        withContext(Dispatchers.IO) {
            SceytChatUIKit.connect(token)
        }

        // Step 2: Wait for connection result and update profile
        val connectionResult = withContext(Dispatchers.IO) {
            ConnectionEventManager.awaitToConnectSceyt()
        }

        if (connectionResult) {
            withContext(Dispatchers.Main) {
                SceytChatUIKit.chatUIFacade.userInteractor.updateProfile(
                    username = "",
                    firstName = KeyStorePref.getString(AppConstants.KEY_STORE_NAME),
                    lastName = "",
                    avatarUrl = null, // Pass your avatar URL here
                    metadataMap = null // Pass metadata if needed
                )
            }
        } else {
            // Handle connection failure if needed
            println("Failed to connect to the chat client.")
        }
    }


    private fun updateDrawerUI() {
        binding.includeDrawerLayout.navHeader.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.includeDrawerLayout.profileName.text =
            KeyStorePref.getString(AppConstants.KEY_STORE_NAME)

        binding.includeDrawerLayout.tvCustomerSupport.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val whatsappNumber = "917827515429"
            Utils.openWhatsApp(this, whatsappNumber)
        }

        binding.includeDrawerLayout.whatsappIcon.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val whatsappNumber = "917827515429"
            Utils.openWhatsApp(this, whatsappNumber)
        }

        binding.includeDrawerLayout.facebookIcon.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val facebookUrl = "https://www.facebook.com/iastroorg"
            val facebookAppUri = "fb://facewebmodal/f?href=$facebookUrl"
            Utils.openLink(this, facebookAppUri, facebookUrl)
        }

        binding.includeDrawerLayout.instagramIcon.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val instagramUrl = "https://www.instagram.com/iastroorg/"
            val instagramAppUri = "http://instagram.com/_u/iastroorg"
            Utils.openLink(this, instagramAppUri, instagramUrl)
        }

        binding.includeDrawerLayout.youtubeIcon.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val youtubeAppUri = "vnd.youtube://channel/UC60rqPVnAkn_t9naR9M81rA"
            val youtubeWebUrl = "https://www.youtube.com/channel/UC60rqPVnAkn_t9naR9M81rA"
            Utils.openLink(this, youtubeAppUri, youtubeWebUrl)
        }

        binding.includeDrawerLayout.tvWallet.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(this, WalletActivity::class.java)
            startActivity(intent)
        }

        binding.includeDrawerLayout.tvOrderHistory.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(this, BookingHistoryActivity::class.java)
            startActivity(intent)
        }

        binding.includeDrawerLayout.tvTalkWithAstrologer.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            binding.bottomNavigation.selectedItemId = R.id.nav_call
        }
    }

    private fun subscribeToZodiacSignTopic() {
        val dateOfBirth = KeyStorePref.getString(AppConstants.KEY_STORE_DOB)
        // Example Date of Birth
        val dob = dateOfBirth?.let { Utils.parseDate(it) }
        val sign = dob?.let { Utils.getSunSign(it) }
        sign?.let {
            FirebaseMessaging.getInstance().subscribeToTopic(it.lowercase())
        }
    }

    private fun updateFcmTokenApi() {
        if (KeyStorePref.getBoolean(AppConstants.KEY_STORE_IS_LOGIN) && !KeyStorePref.getString(
                AppConstants.KEY_STORE_USER_ID
            ).isNullOrEmpty() && !KeyStorePref.getBoolean(AppConstants.KEY_STORE_IS_FCM_TOKEN_SENT)
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                val headerMap = mutableMapOf<String, String>()
                headerMap["Content-Type"] = "application/x-www-form-urlencoded"
                headerMap["Authorization"] =
                    Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)

                val jsonObjectBody = JSONObject()
                jsonObjectBody.put("phone", KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID))
                jsonObjectBody.put(
                    "fcm_token",
                    KeyStorePref.getString(AppConstants.KEY_STORE_FCM_TOKEN)
                )

                OkHttpNetworkProvider.post(
                    BuildConfig.BASE_URL + "UserProfile/update_fcm_token",
                    jsonObjectBody,
                    headerMap,
                    null,
                    null,
                    CommonResponseModel::class.java,
                    object : OkHttpNetworkProvider.NetworkListener<CommonResponseModel> {
                        override fun onResponse(response: CommonResponseModel?) {
                            Log.i(TAG, "onResponse: $response")
                            if (response != null && response.error == false) {
                                KeyStorePref.putBoolean(
                                    AppConstants.KEY_STORE_IS_FCM_TOKEN_SENT,
                                    true
                                )
                            } else {
                                KeyStorePref.putBoolean(
                                    AppConstants.KEY_STORE_IS_FCM_TOKEN_SENT,
                                    false
                                )
                            }
                        }

                        override fun onError(error: BaseErrorModel?) {
                            Log.i(TAG, "onError: ")
                        }
                    })
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if present
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_wallet -> {
                // Handle Settings action
                // Stay on the splash screen for 3 seconds before transitioning to the next screen
                val intent = Intent(this, WalletActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


}
