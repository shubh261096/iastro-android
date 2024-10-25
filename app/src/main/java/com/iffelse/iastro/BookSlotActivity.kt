package com.iffelse.iastro

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.iffelse.iastro.databinding.ActivitySlotBookingsBinding
import com.iffelse.iastro.model.BaseErrorModel
import com.iffelse.iastro.model.response.LoginResponseModel
import com.iffelse.iastro.model.response.WalletResponseModel
import com.iffelse.iastro.model.response.slots.AllBookingsResponseModel
import com.iffelse.iastro.model.response.slots.AllSlotsItem
import com.iffelse.iastro.model.response.slots.AllSlotsResponseModel
import com.iffelse.iastro.model.response.slots.BookingsItem
import com.iffelse.iastro.model.response.slots.TimeSlot
import com.iffelse.iastro.utils.AppConstants
import com.iffelse.iastro.utils.OkHttpNetworkProvider
import com.iffelse.iastro.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalTime

class BookSlotActivity : AppCompatActivity() {

    private var selectedDuration: Int = 0
    private var walletBalance: Double = 0.00
    private var astrologerRate: Double = 0.00
    private var astrologerPhoneNumber: String = ""
    private var slotId: String = ""
    private var selectedStartTime: String = ""


    private lateinit var slotAdapter: SlotAdapter
    private lateinit var binding: ActivitySlotBookingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySlotBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.extras != null) {
            if (intent.hasExtra("final_rate"))
                astrologerRate = intent.getStringExtra("final_rate")?.toDouble()!!
            if (intent.hasExtra("astrologer_phone"))
                astrologerPhoneNumber = intent.getStringExtra("astrologer_phone")!!
        }

        binding.toolbarImage.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        val gridLayoutManagerMinutes = GridLayoutManager(this, 3) // 3 columns
        binding.rvMinutes.layoutManager = gridLayoutManagerMinutes

        val minutesList = listOf(5, 10, 15, 20, 25, 30) // Add more minutes as needed

        val listener = object : MinutesAdapter.OnMinuteSelectedListener {
            override fun onMinuteSelected(minute: Int) {
                selectedDuration = minute
                binding.timeSlotLayout.visibility = View.VISIBLE
                loadAvailableSlots()
                binding.rvTimeSlots.visibility = View.GONE
                binding.tvSlotsTime.visibility = View.GONE
                binding.bookSlotButton.visibility = View.GONE
            }
        }

        val adapter = MinutesAdapter(minutesList, listener)
        binding.rvMinutes.adapter = adapter


        val gridLayoutManagerSlots = GridLayoutManager(this, 3)
        // RecyclerView setup for available slots
        binding.rvAvailableSlots.layoutManager = gridLayoutManagerSlots


        // Book Slot button listener
        binding.bookSlotButton.setOnClickListener {
            if (checkWalletBalance()) {
                bookSlots()
            } else {
                promptUserToAddMoney()
            }
        }

        updateWalletBalanceUI()
    }

    private fun updateWalletBalanceUI() {
        lifecycleScope.launch(Dispatchers.IO) {
            val headers = mutableMapOf<String, String>()
            headers["Content-Type"] = "application/json"
            headers["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)
            OkHttpNetworkProvider.get(BuildConfig.BASE_URL + "wallet/get_balance/" + KeyStorePref.getString(
                AppConstants.KEY_STORE_USER_ID
            ),
                headers,
                null,
                null,
                WalletResponseModel::class.java,
                object : OkHttpNetworkProvider.NetworkListener<WalletResponseModel> {
                    override fun onResponse(response: WalletResponseModel?) {
                        if (response != null) {
                            if (!response.walletBalance.isNullOrEmpty()) {
                                this@BookSlotActivity.walletBalance =
                                    response.walletBalance.toDouble()
                                // Update wallet balance text view
                                lifecycleScope.launch(Dispatchers.Main) {
                                    binding.tvWalletBalance.text =
                                        buildString {
                                            append("Wallet Balance: Rs. ")
                                            append(response.walletBalance)
                                        }
                                }
                            }
                        }
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                    }
                })
        }
    }

    private fun loadAvailableSlots() {
        lifecycleScope.launch(Dispatchers.IO) {
            val headers = mutableMapOf<String, String>()
            headers["Content-Type"] = "application/json"
            headers["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)
            OkHttpNetworkProvider.get(BuildConfig.BASE_URL + "astrologer/all_slots/$astrologerPhoneNumber",
                headers,
                null,
                null,
                AllSlotsResponseModel::class.java,
                object : OkHttpNetworkProvider.NetworkListener<AllSlotsResponseModel> {
                    override fun onResponse(response: AllSlotsResponseModel?) {
                        if (response != null) {
                            if (response.error == false) {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    slotAdapter = SlotAdapter { slot ->
                                        binding.rvTimeSlots.visibility = View.VISIBLE
                                        loadBookedSlotsBySlotId(slot)
                                    }
                                    binding.rvAvailableSlots.adapter = slotAdapter
                                    slotAdapter.updateSlots(response.allSlots)
                                }
                            }
                        }
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                    }
                })
        }
    }

    private fun loadBookedSlotsBySlotId(slotsItem: AllSlotsItem) {
        lifecycleScope.launch(Dispatchers.IO) {
            val headerMap = mutableMapOf<String, String>()
            headerMap["Content-Type"] = "application/x-www-form-urlencoded"
            headerMap["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)

            val jsonObjectBody = JSONObject()
            jsonObjectBody.put("astrologer_phone", slotsItem.astrologerPhone)
            jsonObjectBody.put("slot_id", slotsItem.slotId)

            OkHttpNetworkProvider.post(BuildConfig.BASE_URL + "astrologer/astrologers_bookings",
                jsonObjectBody,
                headerMap,
                null,
                null,
                AllBookingsResponseModel::class.java,
                object : OkHttpNetworkProvider.NetworkListener<AllBookingsResponseModel> {
                    override fun onResponse(response: AllBookingsResponseModel?) {
                        if (response != null) {
                            Log.i(TAG, "onResponse: $response")
                            lifecycleScope.launch(Dispatchers.Main) {
                                setupTimeSlotsAdapter(response.bookings, slotsItem)
                            }
                        }
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                    }

                })
        }
    }

    private fun setupTimeSlotsAdapter(selectedSlot: List<BookingsItem?>?, slotsItem: AllSlotsItem) {
        // Clear the adapter's data to prevent memory leaks
        binding.rvTimeSlots.adapter = null
        binding.tvSlotsTime.visibility = View.VISIBLE

        // Generate time intervals based on the selected slot
        val intervalMinutes = selectedDuration // Default to 5 minutes
        val availableTimeSlots = generateTimeSlots(
            slotsItem.startTime!!,
            slotsItem.endTime!!,
            intervalMinutes,
            selectedSlot ?: emptyList() // Pass an empty list if selectedSlot is null
        )

        // Set up the TimeSlotsAdapter
        val timeSlotsAdapter = TimeSlotsAdapter(availableTimeSlots) { selectedTimeSlot ->

            this.selectedStartTime = "${selectedTimeSlot.startTime}:00"
            this.slotId = slotsItem.slotId!!
            Log.i(TAG, "setupTimeSlotsAdapter: ${selectedTimeSlot.startTime}:00")
            // Update UI with selected time frame
            binding.tvSlotsTime.visibility = View.VISIBLE
            binding.bookSlotButton.visibility = View.VISIBLE
        }

        val gridLayoutManagerMinutes = GridLayoutManager(this, 4) // 3 columns
        binding.rvTimeSlots.layoutManager = gridLayoutManagerMinutes
        binding.rvTimeSlots.adapter = timeSlotsAdapter
    }


    private fun bookSlots() {
        lifecycleScope.launch(Dispatchers.IO) {
            if (this@BookSlotActivity.astrologerPhoneNumber.isEmpty()) {
                return@launch
            }

            if (this@BookSlotActivity.slotId.isEmpty()) {
                return@launch
            }

            if (this@BookSlotActivity.selectedDuration < 0) {
                Toast.makeText(
                    this@BookSlotActivity,
                    "Please select the minutes",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            if (this@BookSlotActivity.selectedStartTime.isEmpty()) {
                Toast.makeText(this@BookSlotActivity, "Please select the slot", Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }


            val headerMap = mutableMapOf<String, String>()
            headerMap["Content-Type"] = "application/json"
            headerMap["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)

            val jsonObjectBody = JSONObject()
            jsonObjectBody.put("astrologer_phone", this@BookSlotActivity.astrologerPhoneNumber)
            jsonObjectBody.put("slot_id", this@BookSlotActivity.slotId)
            jsonObjectBody.put("user_phone", KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID))
            jsonObjectBody.put("call_duration_minutes", this@BookSlotActivity.selectedDuration)
            jsonObjectBody.put(
                "total_cost",
                (this@BookSlotActivity.selectedDuration * this@BookSlotActivity.astrologerRate)
            )
            jsonObjectBody.put("booked_start_time", this@BookSlotActivity.selectedStartTime)

            OkHttpNetworkProvider.post(BuildConfig.BASE_URL + "booking/book_slot",
                jsonObjectBody,
                headerMap,
                null,
                null,
                LoginResponseModel::class.java,
                object : OkHttpNetworkProvider.NetworkListener<LoginResponseModel> {
                    override fun onResponse(response: LoginResponseModel?) {
                        if (response != null) {
                            Log.i(TAG, "onResponse: $response")
                            lifecycleScope.launch(Dispatchers.Main) {
                                Toast.makeText(
                                    this@BookSlotActivity,
                                    response.message ?: "Something went wrong",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(this@BookSlotActivity, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }

                        }
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                    }
                })
        }
    }

    // Function to generate time intervals
    @SuppressLint("NewApi")
    private fun generateTimeSlots(
        startTime: String,
        endTime: String,
        intervalMinutes: Int,
        bookedSlots: List<BookingsItem?> // Accept booked slots list
    ): List<TimeSlot> {
        val timeSlots = mutableListOf<TimeSlot>()
        var currentTime = LocalTime.parse(startTime)
        val finalTime = LocalTime.parse(endTime)

        // Avoid generating slots if intervalMinutes is invalid or start is after end
        if (intervalMinutes <= 0 || currentTime.isAfter(finalTime)) {
            return timeSlots
        }

        // Add a check to limit the number of time slots generated
        val maxSlots = 100 // Adjust this number based on your needs
        var slotCount = 0

        while (currentTime.isBefore(finalTime) && slotCount < maxSlots) {
            val nextTime = currentTime.plusMinutes(intervalMinutes.toLong())

            // Check if the time slot is booked
            val isBooked = bookedSlots.any { bookedSlot ->
                bookedSlot?.let {
                    val bookedStart = LocalTime.parse(it.bookedStartTime)
                    val bookedEnd = LocalTime.parse(it.bookedEndTime)
                    bookedStart.isBefore(nextTime) && bookedEnd.isAfter(currentTime)
                } ?: false
            }

            // Add each time interval as a time slot (without calling toString() repeatedly)
            timeSlots.add(TimeSlot(currentTime.toString(), nextTime.toString(), isBooked))

            // Move to the next time slot
            currentTime = nextTime
            slotCount++

            // Break if the time goes beyond the final time
            if (nextTime.isAfter(finalTime)) break
        }


        return timeSlots
    }


    private fun checkWalletBalance(): Boolean {
        if ((astrologerRate * selectedDuration) > walletBalance) {
            Toast.makeText(this, "Please add money", Toast.LENGTH_SHORT).show()
            return false
        }
        // Logic to check if the user has enough balance
        return true // Replace with actual balance check
    }


    private fun promptUserToAddMoney() {
        // Show a dialog or redirect user to add money in the wallet
    }

    companion object {
        private const val TAG = "BookSlotActivity"
    }
}
