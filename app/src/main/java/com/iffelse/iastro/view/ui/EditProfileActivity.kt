package com.iffelse.iastro.view.ui


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.iffelse.iastro.BuildConfig
import com.iffelse.iastro.databinding.ActivityEditProfileBinding
import com.iffelse.iastro.model.BaseErrorModel
import com.iffelse.iastro.model.response.LoginResponseModel
import com.iffelse.iastro.model.response.User
import com.iffelse.iastro.utils.AppConstants
import com.iffelse.iastro.utils.KeyStorePref
import com.iffelse.iastro.utils.OkHttpNetworkProvider
import com.iffelse.iastro.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class EditProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityEditProfileBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.toolbarImage.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Update wallet balance UI
        updateWalletBalanceUI()

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.toolbarImage.callOnClick()
            }
        })
    }

    private fun updateWalletBalanceUI() {
        Utils.showProgress(this@EditProfileActivity, "Please wait...")
        lifecycleScope.launch(Dispatchers.IO) {
            val headerMap = mutableMapOf<String, String>()
            headerMap["Content-Type"] = "application/x-www-form-urlencoded"

            val jsonObjectBody = JSONObject()
            jsonObjectBody.put("phone", KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID))

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
                            if (response != null) {
                                Log.i(TAG, "onResponse: $response")
                                if (response.error == false) {
                                    // Check if user is new or old
                                    if (response.user != null) {
                                        updateUI(response.user)
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
                                this@EditProfileActivity,
                                error?.message ?: "Something went wrong!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                })
        }
    }

    private fun updateUI(user: User) {
        binding.etName.setText(user.name)
        binding.etPhone.setText(user.phoneNumber)
        binding.etDob.setText(user.dob)
        binding.etGender.setText(user.gender)
        binding.etTime.setText(user.timeOfBirth)
        user.email?.let {
            binding.etEmail.visibility = View.VISIBLE
            binding.etEmail.setText(user.email)
        }
    }

    companion object {
        private const val TAG = "EditProfileActivity"
    }
}
