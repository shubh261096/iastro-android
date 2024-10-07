package com.iffelse.iastro

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseHelper {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef: DatabaseReference = database.getReference("users")

    // Call this method after successful login to store user data.
    fun saveFormSubmission(userId: String, formData: FormData) {
        // Generate a new submission ID for each form
        val submissionId = usersRef.child(userId).child("formSubmissions").push().key

        // Save form data under the user ID
        if (submissionId != null) {
            usersRef.child(userId)
                .child("formSubmissions")
                .child(submissionId)
                .setValue(formData)
                .addOnSuccessListener {
                    // Data successfully written to Firebase
                }
                .addOnFailureListener {
                    // Failed to write data
                }
        }
    }

    // If you want to store user profile data
    fun saveUserProfile(userId: String, profileData: UserProfile) {
        usersRef.child(userId).child("profileData").setValue(profileData)
    }

    fun checkIfNameExists(userId: String, onResult: (Boolean, DataSnapshot?) -> Unit) {
        val database = usersRef.child(userId).child("profileData")

        // Attach a listener to read the data at the userId node
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Check if the "name" field exists for the user
                    val name = dataSnapshot.child("name").getValue(String::class.java)
                    if (!name.isNullOrEmpty()) {
                        // Name exists and is not empty
                        onResult(true, dataSnapshot)
                    } else {
                        // Name does not exist
                        onResult(false, null)
                    }
                } else {
                    // User ID does not exist in the database
                    onResult(false, null)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Failed to read the database
                onResult(false, null)
            }
        })
    }
}

// Define your form data model
data class FormData(
    val name: String = "",
    val email: String = "",
    val message: String = "",
    val timestamp: String = getFormattedTimestamp() // Add other fields
)

fun getFormattedTimestamp(): String {
    val sdf = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
    return sdf.format(Date())
}

// Define your user profile model if needed
data class UserProfile(
    val phoneNumber: String, // Other fields you want to store
    val name: String = "",
    val gender: String = "",
    val dob: String = "",
    val time: String = "",
    val placeOfBirth: String = "",


    )
