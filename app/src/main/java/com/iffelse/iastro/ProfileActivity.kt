package com.iffelse.iastro

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import com.iffelse.iastro.databinding.ActivityProfileBinding
import com.iffelse.iastro.utils.Utils

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

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
                .setItems(genderOptions) { dialog: DialogInterface, which: Int ->
                    // 'which' is the index of the selected item
                    binding.etGender.setText(genderOptions[which])
                }
                .setNegativeButton("Cancel", null) // Add cancel option
                .show()
        }


        binding.btnSubmit.setOnClickListener {

            val name = binding.etName.text.toString().trim()
            val gender = binding.etGender.text.toString().trim()
            val dob = binding.etDob.text.toString().trim()
            val time = binding.etTimeOfBirth.text.toString().trim()
            val placeOfBirth = binding.etPlace.text.toString().trim()

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

            val firebaseHelper = FirebaseHelper()
            firebaseHelper.saveUserProfile(
                KeyStorePref.getString("userId")!!,
                UserProfile(
                    phoneNumber = KeyStorePref.getString("userId")!!,
                    name = name,
                    gender = gender,
                    dob = dob,
                    time = time,
                    placeOfBirth = placeOfBirth
                )
            )

            KeyStorePref.putString("name", binding.etName.text.toString().trim())
            KeyStorePref.putString("dob", binding.etDob.text.toString().trim())

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }


    }
}