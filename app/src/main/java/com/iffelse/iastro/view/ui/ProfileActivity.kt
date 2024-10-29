package com.iffelse.iastro.view.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.iffelse.iastro.BuildConfig
import com.iffelse.iastro.R
import com.iffelse.iastro.databinding.ActivityProfileBinding
import com.iffelse.iastro.model.BaseErrorModel
import com.iffelse.iastro.model.response.LoginResponseModel
import com.iffelse.iastro.utils.AppConstants
import com.iffelse.iastro.utils.KeyStorePref
import com.iffelse.iastro.utils.OkHttpNetworkProvider
import com.iffelse.iastro.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private val TAG = "ProfileActivity"

    private lateinit var convertedTime: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using ViewBinding
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etDob.setOnClickListener {
            Utils.openDatePicker(
                null,
                null,
                null,
                this@ProfileActivity,
                object : Utils.DateFormatResult {
                    override fun onDateSelected(date: String) {
                        binding.etDob.setText(date)
                    }
                })
        }

        binding.etTimeOfBirth.setOnClickListener {
            Utils.openTimePicker(this@ProfileActivity,
                object : Utils.DateFormatResult {
                    override fun onDateSelected(date: String) {
                        convertedTime = Utils.convertTo24HourFormat(date)
                        binding.etTimeOfBirth.setText(date)
                    }
                })
        }


        // Set an OnClickListener for the EditText
        binding.etGender.setOnClickListener {
            // Create an AlertDialog to show gender options
            val genderOptions = resources.getStringArray(R.array.gender_array)

            AlertDialog.Builder(this)
                .setTitle("Select Gender")
                .setItems(genderOptions) { _: DialogInterface, which: Int ->
                    // 'which' is the index of the selected item
                    binding.etGender.setText(genderOptions[which])
                }
                .setNegativeButton("Cancel", null) // Add cancel option
                .show()
        }

        // Set OnClickListener for the EditText to select preferred languages
        binding.etLanguage.setOnClickListener {
            // Inflate the custom layout
            val dialogView: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_languages, null)

            // Find views inside the custom layout
            val cbHindi = dialogView.findViewById<CheckBox>(R.id.cbHindi)
            val cbEnglish = dialogView.findViewById<CheckBox>(R.id.cbEnglish)
            val cbOthers = dialogView.findViewById<CheckBox>(R.id.cbOthers)
            val etOtherLanguage = dialogView.findViewById<EditText>(R.id.etOtherLanguage)

            // Handle the visibility of the EditText when "Others" is checked
            cbOthers.setOnCheckedChangeListener { _, isChecked ->
                etOtherLanguage.visibility = if (isChecked) View.VISIBLE else View.GONE
            }

            // Create and show the dialog
            AlertDialog.Builder(this)
                .setTitle("Select Preferred Language")
                .setView(dialogView) // Set the custom layout
                .setPositiveButton("OK") { _, _ ->
                    val selectedLanguages = mutableListOf<String>()

                    // Collect selected languages
                    if (cbHindi.isChecked) selectedLanguages.add("Hindi")
                    if (cbEnglish.isChecked) selectedLanguages.add("English")
                    if (cbOthers.isChecked) {
                        val customLanguage = etOtherLanguage.text.toString().trim()
                        // Split by commas, spaces, or any non-letter characters using regex
                        if (customLanguage.isNotEmpty()) {
                            val languagesList =
                                customLanguage.split(Regex("\\s+|,\\s*")).filter { it.isNotEmpty() }
                            selectedLanguages.addAll(languagesList)
                        }
                    }

                    // Update the EditText with selected languages
                    binding.etLanguage.setText(selectedLanguages.joinToString(", "))
                }
                .setNegativeButton("Cancel", null)
                .show()
        }


        binding.btnSubmit.setOnClickListener {

            val name = binding.etName.text.toString().trim()
            val gender = binding.etGender.text.toString().trim()
            val dob = binding.etDob.text.toString().trim()
            val time = binding.etTimeOfBirth.text.toString().trim()
            val placeOfBirth = binding.etPlace.text.toString().trim()
            val language = binding.etLanguage.text.toString().trim()
            // Validation
            if (name.isEmpty()) {
                binding.etName.error = "Name is required"
                return@setOnClickListener
            }

            if (gender.isEmpty()) {
                binding.etGender.error = "Gender is required"
                return@setOnClickListener
            }

            if (dob.isEmpty()) {
                binding.etDob.error = "Date of Birth is required"
                return@setOnClickListener
            }

            if (time.isEmpty()) {
                binding.etTimeOfBirth.error = "Time of Birth is required"
                return@setOnClickListener
            }

            if (placeOfBirth.isEmpty()) {
                binding.etPlace.error = "Place of Birth is required"
                return@setOnClickListener
            }

            if (language.isEmpty()) {
                binding.etLanguage.error = "Language is required"
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val headers = mutableMapOf<String, String>()
                headers["Content-Type"] = "application/x-www-form-urlencoded"
                headers["Authorization"] =
                    Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)

                val jsonObjectBody = JSONObject()
                jsonObjectBody.put(
                    "phone",
                    KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!
                )
                jsonObjectBody.put("name", name)
                jsonObjectBody.put("dob", dob)
                jsonObjectBody.put("time_of_birth", convertedTime)
                jsonObjectBody.put("place_of_birth", placeOfBirth)
                jsonObjectBody.put("preferred_languages", language)
                jsonObjectBody.put("gender", gender)

                OkHttpNetworkProvider.post(
                    BuildConfig.BASE_URL + "/UserProfile/update_user",
                    jsonObjectBody,
                    headers,
                    null,
                    null,
                    LoginResponseModel::class.java,
                    object : OkHttpNetworkProvider.NetworkListener<LoginResponseModel> {
                        override fun onResponse(response: LoginResponseModel?) {
                            if (response != null) {
                                if (response.error == false) {
                                    KeyStorePref.putString(AppConstants.KEY_STORE_NAME, name)
                                    KeyStorePref.putString(AppConstants.KEY_STORE_DOB, dob)
                                    val intent = Intent(this@ProfileActivity, HomeActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                            Log.i(TAG, "onResponse: $response")
                        }

                        override fun onError(error: BaseErrorModel?) {
                            Log.i(TAG, "onError: ")
                            // TODO: Handle Error
                        }

                    })
            }
        }


    }
}