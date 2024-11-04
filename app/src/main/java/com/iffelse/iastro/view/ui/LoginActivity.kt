package com.iffelse.iastro.view.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.iffelse.iastro.BuildConfig
import com.iffelse.iastro.R
import com.iffelse.iastro.databinding.ActivityLoginBinding
import com.iffelse.iastro.model.BaseErrorModel
import com.iffelse.iastro.model.response.LoginResponseModel
import com.iffelse.iastro.utils.AppConstants
import com.iffelse.iastro.utils.KeyStorePref
import com.iffelse.iastro.utils.OkHttpNetworkProvider
import com.iffelse.iastro.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val TAG = "LoginActivity"


    private val otp = (100000..999999).random() // Generates a number between 100000 and 999999

    private var isTestNumber = false


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        KeyStorePref.initialize(applicationContext)

        // Load rotate animation from XML and apply to the rotating image
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_logo)
        binding.ivLogo.startAnimation(rotateAnimation)


        // Handle Send OTP Button Click
        binding.btnSendOtp.setOnClickListener {
            if (binding.etMobileNumber.text.toString().trim().isEmpty()) {
                Toast.makeText(this@LoginActivity, "Please enter phone number", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            // Play fade-out animation and hide login section
            val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
            binding.layoutLogin.startAnimation(fadeOut)
            binding.layoutLogin.visibility = View.GONE

            // Play fade-in animation and show OTP section
            val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            binding.layoutOtp.startAnimation(fadeIn)
            binding.layoutOtp.visibility = View.VISIBLE

            binding.tvTermsConditions.visibility = View.GONE

            if (binding.etMobileNumber.text.toString().trim() == "1111111100") {
                isTestNumber = true
                return@setOnClickListener
            }
            Utils.showProgress(this@LoginActivity, "Please wait...")

            val jsonObject = JSONObject(
                "{" +
                        "    \"extra\": {" +
                        "        \"dltContentId\": \"1707172855535386545\"" +
                        "    }," +
                        "    \"message\": {" +
                        "        \"recipient\": 91${
                            binding.etMobileNumber.text.toString().trim()
                        }," +
                        "        \"text\": \"Dear Customer. Login to iAstro using OTP $otp LZYCLK\"" +
                        "    }," +
                        "    \"sender\": \"LAZYCL\"," +
                        "    \"unicode\": \"False\"" +
                        "}"
            )
            val headers = mutableMapOf<String, String>()
            headers["Content-Type"] = "application/json"
            headers["Authorization"] = "Basic c3BpcmFlYW90cC50cmFuczpKNXlrbg=="
            Log.i("TAG", "onCreate: $otp")
            val url = "https://sms.timesapi.in/api/v1/message"

            lifecycleScope.launch(Dispatchers.IO) {
                OkHttpNetworkProvider.post(url,
                    jsonObject,
                    headers,
                    null,
                    null,
                    responseType = JSONObject::class.java,
                    object : OkHttpNetworkProvider.NetworkListener<JSONObject> {
                        override fun onResponse(response: JSONObject?) {
                            lifecycleScope.launch(Dispatchers.Main) {
                                Utils.hideProgress()
                            }
                            Log.i("TAG", "onResponse: ${response.toString()}")
                        }

                        override fun onError(error: BaseErrorModel?) {
                            lifecycleScope.launch(Dispatchers.Main) {
                                Utils.hideProgress()
                                Toast.makeText(
                                    this@LoginActivity,
                                    error?.message,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    })
            }

        }

        // Handle Verify OTP Button Click (Add logic here)
        binding.btnVerifyOtp.setOnClickListener {
            if (binding.etOtp.text.toString().trim().isEmpty()) {
                Toast.makeText(this@LoginActivity, "Please enter otp", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            Utils.showProgress(this@LoginActivity, "Please wait...")
            if (binding.etOtp.text.toString().trim() == otp.toString() || isTestNumber) {
//                Toast.makeText(this@LoginActivity, "Otp Verified", Toast.LENGTH_SHORT).show()

                lifecycleScope.launch(Dispatchers.IO) {
                    val headerMap = mutableMapOf<String, String>()
                    headerMap["Content-Type"] = "application/x-www-form-urlencoded"

                    val jsonObjectBody = JSONObject()
                    jsonObjectBody.put(
                        "phone",
                        "91" + binding.etMobileNumber.text.toString().trim()
                    )

                    OkHttpNetworkProvider.post(
                        BuildConfig.BASE_URL + "login",
                        jsonObjectBody,
                        headerMap,
                        null,
                        null,
                        LoginResponseModel::class.java,
                        object : OkHttpNetworkProvider.NetworkListener<LoginResponseModel> {
                            override fun onResponse(response: LoginResponseModel?) {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    Utils.hideProgress()
                                }
                                if (response != null) {
                                    Log.i(TAG, "onResponse: $response")
                                    if (response.error == false) {
                                        KeyStorePref.putString(
                                            AppConstants.KEY_STORE_USER_ID,
                                            "91" + binding.etMobileNumber.text.toString().trim()
                                        )
                                        KeyStorePref.putBoolean(
                                            AppConstants.KEY_STORE_IS_LOGIN,
                                            true
                                        )
                                        // Check if user is new or old
                                        if (response.userStatus == "new") {
                                            val intent = Intent(
                                                this@LoginActivity,
                                                ProfileActivity::class.java
                                            )
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            if (response.user != null) {
                                                if (!response.user.name.isNullOrEmpty()) {
                                                    KeyStorePref.putString(
                                                        AppConstants.KEY_STORE_NAME,
                                                        response.user.name
                                                    )
                                                }

                                                if (!response.user.dob.isNullOrEmpty()) {
                                                    KeyStorePref.putString(
                                                        AppConstants.KEY_STORE_DOB,
                                                        response.user.dob
                                                    )
                                                }
                                                val intent =
                                                    Intent(
                                                        this@LoginActivity,
                                                        HomeActivity::class.java
                                                    )
                                                startActivity(intent)
                                                finish()
                                            }

                                        }
                                    }
                                }
                            }

                            override fun onError(error: BaseErrorModel?) {
                                Log.i(TAG, "onError: ")
                                Utils.hideProgress()
                                lifecycleScope.launch(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        error?.message,
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }
                        })
                }
            } else {
                Toast.makeText(this@LoginActivity, "Otp Not Verified", Toast.LENGTH_SHORT).show()
            }
        }
        setupClickableText()

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })
    }

    private fun setupClickableText() {
        val text = "By continuing, you agree to our Terms of use & Privacy Policy"

        // Create a SpannableString from the text
        val spannableString = SpannableString(text)

        // Create clickable span for "Terms and Conditions"
        val termsClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Open Terms and Conditions URL
                val termsUrl = "https://www.iastro.org/terms.html"
                openUrl(termsUrl)
            }
        }

        // Create clickable span for "Privacy Policy"
        val privacyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Open Privacy Policy URL
                val privacyUrl = "https://www.iastro.org/privacy.html"
                openUrl(privacyUrl)
            }
        }

        // Set clickable spans for specific parts of the text
        val termsStart = text.indexOf("Terms of use")
        val termsEnd = termsStart + "Terms of use".length
        spannableString.setSpan(
            termsClickableSpan,
            termsStart,
            termsEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.orange)),
            termsStart,
            termsEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val privacyStart = text.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length
        spannableString.setSpan(
            privacyClickableSpan,
            privacyStart,
            privacyEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.orange)),
            privacyStart,
            privacyEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )


        // Set the spannable string to the TextView and enable movement method to handle clicks
        binding.tvTermsConditions.text = spannableString
        binding.tvTermsConditions.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}