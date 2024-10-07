package com.iffelse.iastro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.iffelse.iastro.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using ViewBinding
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmit.setOnClickListener {
            val firebaseHelper = FirebaseHelper()
            firebaseHelper.saveUserProfile(
                KeyStorePref.getString("userId")!!,
                UserProfile(
                    phoneNumber = KeyStorePref.getString("userId")!!,
                    name =  binding.etName.text.toString().trim(),
                    gender =  binding.etGender.text.toString().trim(),
                    dob = binding.etDob.text.toString().trim(),
                    time = binding.etTimeOfBirth.text.toString().trim(),
                    placeOfBirth = binding.etPlace.text.toString().trim()
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