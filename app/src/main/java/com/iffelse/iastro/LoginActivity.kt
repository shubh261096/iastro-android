package com.iffelse.iastro

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.iffelse.iastro.databinding.ActivityLoginBinding
import com.iffelse.iastro.model.BaseErrorModel
import com.iffelse.iastro.utils.OkHttpNetworkProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Base64
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val firebaseHelper = FirebaseHelper()


    private val otp = (100000..999999).random() // Generates a number between 100000 and 999999


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        KeyStorePref.initialize(applicationContext)


        // Handle Send OTP Button Click
        binding.btnSendOtp.setOnClickListener {
            // Play fade-out animation and hide login section
            val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
            binding.layoutLogin.startAnimation(fadeOut)
            binding.layoutLogin.visibility = View.GONE

            // Play fade-in animation and show OTP section
            val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            binding.layoutOtp.startAnimation(fadeIn)
            binding.layoutOtp.visibility = View.VISIBLE

            val jsonObject = JSONObject(
                "{" +
                        "    \"extra\": {" +
                        "        \"dltContentId\": \"1707166755462671638\"" +
                        "    }," +
                        "    \"message\": {" +
                        "        \"recipient\": 91${
                            binding.etMobileNumber.text.toString().trim()
                        }," +
                        "        \"text\": \"Dear Customer. Login to WeCredit using OTP $otp. WeCredit(SPIRAEA)\"" +
                        "    }," +
                        "    \"sender\": \"SPRAEA\"," +
                        "    \"unicode\": \"true\"" +
                        "}"
            )
            val headers = mutableMapOf<String, String>()
            headers.put("Content-Type", "application/json")
            headers.put("Authorization", getBasicAuthHeader("spiraeaotp.trans", "J5ykn"))
            Log.i("TAG", "onCreate: $otp")
            val url = "https://sms.timesapi.in"

            lifecycleScope.launch(Dispatchers.IO) {
                OkHttpNetworkProvider.post(url,
                    jsonObject,
                    headers,
                    null,
                    null,
                    responseType = JSONObject::class.java,
                    object : OkHttpNetworkProvider.NetworkListener<JSONObject> {
                        override fun onResponse(response: JSONObject?) {
                            Log.i("TAG", "onResponse: ${response.toString()}")
                        }

                        override fun onError(error: BaseErrorModel?) {

                        }

                    })
            }

        }

        // Handle Verify OTP Button Click (Add logic here)
        binding.btnVerifyOtp.setOnClickListener {
            if (binding.etOtp.text.toString().trim() == otp.toString()) {
                Toast.makeText(this@LoginActivity, "Otp Verified", Toast.LENGTH_SHORT).show()

                val userId = binding.etMobileNumber.text.toString()
                    .trim() // Replace with actual phone number or user ID
                firebaseHelper.checkIfUserExists(userId) { isUser, dataSnapShot ->
                    KeyStorePref.putString("userId", userId)
                    KeyStorePref.putBoolean("isLogin", true)
                    if (isUser) {
                        firebaseHelper.checkIfNameExists(userId) { hasName, dataSnapShot ->
                            if (hasName) {
                                val intent = Intent(this, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                val intent = Intent(this, ProfileActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                    } else {
                        firebaseHelper.saveUserProfile(
                            KeyStorePref.getString("userId")!!,
                            UserProfile(
                                phoneNumber = KeyStorePref.getString("userId")!!,
                                "", "", "", "", ""
                            )
                        )
                        val intent = Intent(this, ProfileActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                }


            } else {
                Toast.makeText(this@LoginActivity, "Otp Not Verified", Toast.LENGTH_SHORT).show()
            }


        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getBasicAuthHeader(username: String, password: String): String {
        val credentials = "$username:$password"
        val basicAuth = "Basic " + Base64.getEncoder().encodeToString(credentials.toByteArray())
        return basicAuth
    }
}