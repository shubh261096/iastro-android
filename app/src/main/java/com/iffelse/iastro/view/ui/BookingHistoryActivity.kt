package com.iffelse.iastro.view.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.iffelse.iastro.BuildConfig
import com.iffelse.iastro.databinding.ActivityBookingHistoryBinding
import com.iffelse.iastro.model.BaseErrorModel
import com.iffelse.iastro.model.response.BookingsHistoryResponseModel
import com.iffelse.iastro.utils.AppConstants
import com.iffelse.iastro.utils.KeyStorePref
import com.iffelse.iastro.utils.OkHttpNetworkProvider
import com.iffelse.iastro.utils.Utils
import com.iffelse.iastro.view.adapter.BookingHistoryAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookingHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingHistoryBinding
    private lateinit var bookingHistoryAdapter: BookingHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBookingHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.toolbarImage.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.recyclerViewBookings.layoutManager =
            LinearLayoutManager(this@BookingHistoryActivity)


        // Fetch form submissions
        fetchFormSubmissions()

        binding.titleConsultNow.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun fetchFormSubmissions() {
        Utils.showProgress(this@BookingHistoryActivity, "Please wait...")
        lifecycleScope.launch(Dispatchers.IO) {
            val headers = mutableMapOf<String, String>()
            headers["Content-Type"] = "application/json"
            headers["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)
            OkHttpNetworkProvider.get(
                BuildConfig.BASE_URL + "booking/booking_history/" + KeyStorePref.getString(
                    AppConstants.KEY_STORE_USER_ID
                ),
                headers,
                null,
                null,
                BookingsHistoryResponseModel::class.java,
                object : OkHttpNetworkProvider.NetworkListener<BookingsHistoryResponseModel> {
                    override fun onResponse(response: BookingsHistoryResponseModel?) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Utils.hideProgress()
                        }
                        if (response != null) {
                            lifecycleScope.launch(Dispatchers.Main) {

                                if (!response.bookingsHistory.isNullOrEmpty()) {

                                    bookingHistoryAdapter =
                                        BookingHistoryAdapter(response.bookingsHistory)
                                    binding.recyclerViewBookings.adapter = bookingHistoryAdapter
                                    binding.noBookingLayout.visibility = View.GONE
                                    binding.recyclerViewBookings.visibility = View.VISIBLE
                                } else {
                                    binding.noBookingLayout.visibility = View.VISIBLE
                                    binding.recyclerViewBookings.visibility = View.GONE
                                }
                            }
                        }
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                        lifecycleScope.launch(Dispatchers.Main) {
                            Utils.hideProgress()
                            Toast.makeText(
                                this@BookingHistoryActivity,
                                error?.message,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                })
        }
    }

    companion object {
        private const val TAG = "BookingHistoryActivity"
    }

}
