package com.iffelse.iastro.view.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.iffelse.iastro.view.adapter.BookingHistoryAdapter
import com.iffelse.iastro.utils.KeyStorePref
import com.iffelse.iastro.databinding.ActivityBookingHistoryBinding
import com.iffelse.iastro.model.FormSubmission

class BookingHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingHistoryBinding
    private lateinit var bookingHistoryAdapter: BookingHistoryAdapter
    private val formSubmissionList = mutableListOf<FormSubmission>()

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef: DatabaseReference = database.getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBookingHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.toolbarImage.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Set up RecyclerView
        binding.recyclerViewBookings.layoutManager = LinearLayoutManager(this)
        bookingHistoryAdapter = BookingHistoryAdapter(formSubmissionList)


        // Fetch form submissions
        fetchFormSubmissions()

        binding.titleConsultNow.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun fetchFormSubmissions() {
        val database = usersRef.child(KeyStorePref.getString("userId")!!).child("formSubmissions")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                formSubmissionList.clear()  // Clear the list before adding new data
                for (submissionSnapshot in snapshot.children) {
                    val formSubmission = submissionSnapshot.getValue(FormSubmission::class.java)
                    if (formSubmission != null) {
                        formSubmissionList.add(formSubmission)
                    }
                }

                if (formSubmissionList.size > 0) {
                    // Reverse the list so that the latest items appear first
                    formSubmissionList.reverse()
                    binding.noBookingLayout.visibility = View.GONE
                    binding.recyclerViewBookings.visibility = View.VISIBLE
                } else {
                    binding.noBookingLayout.visibility = View.VISIBLE
                    binding.recyclerViewBookings.visibility = View.GONE
                }

                binding.recyclerViewBookings.adapter = bookingHistoryAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching data: ${error.message}")
            }
        })
    }

}
