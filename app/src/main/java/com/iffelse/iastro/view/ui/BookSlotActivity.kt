package com.iffelse.iastro.view.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.iffelse.iastro.BuildConfig
import com.iffelse.iastro.R
import com.iffelse.iastro.databinding.ActivitySlotBookingsBinding
import com.iffelse.iastro.model.BaseErrorModel
import com.iffelse.iastro.model.response.AstrologerStatusResponseModel
import com.iffelse.iastro.model.response.ChatTokenResponseModel
import com.iffelse.iastro.model.response.LoginResponseModel
import com.iffelse.iastro.model.response.WalletResponseModel
import com.iffelse.iastro.model.response.slots.AllBookingsResponseModel
import com.iffelse.iastro.model.response.slots.AllSlotsItem
import com.iffelse.iastro.model.response.slots.AllSlotsResponseModel
import com.iffelse.iastro.model.response.slots.BookingsItem
import com.iffelse.iastro.model.response.slots.TimeSlot
import com.iffelse.iastro.utils.AppConstants
import com.iffelse.iastro.utils.KeyStorePref
import com.iffelse.iastro.utils.OkHttpNetworkProvider
import com.iffelse.iastro.utils.Utils
import com.iffelse.iastro.view.adapter.MinutesAdapter
import com.iffelse.iastro.view.adapter.SlotAdapter
import com.iffelse.iastro.view.adapter.TimeSlotsAdapter
import com.iffelse.iastro.view.adapter.TypeAdapter
import com.sceyt.chatuikit.SceytChatUIKit
import com.sceyt.chatuikit.data.managers.connection.ConnectionEventManager
import com.sceyt.chatuikit.data.models.SceytResponse
import com.sceyt.chatuikit.data.models.channels.CreateChannelData
import com.sceyt.chatuikit.data.models.channels.SceytMember
import com.sceyt.chatuikit.data.models.messages.SceytUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalTime
import java.util.Calendar


class BookSlotActivity : BaseActivity() {

    private var selectedDuration: Int = 0
    private var walletBalance: Double = 0.00
    private var astrologerRate: Double = 0.00
    private var astrologerPhoneNumber: String = ""
    private var slotId: String = ""
    private var selectedStartTime: String = ""
    private var isFreeUser: Boolean = false
    private var type: String = "chat"


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
            if (intent.hasExtra("type"))
                type = intent.getStringExtra("type")!!
        }

        binding.toolbarImage.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }


        val gridLayoutManagerSlots = GridLayoutManager(this, 3)
        // RecyclerView setup for available slots
        binding.rvAvailableSlots.layoutManager = gridLayoutManagerSlots

        // Chat Button Listener
        binding.chatButton.setOnClickListener {
            if (this.isFreeUser) {
                bookSlots("chat")
            } else {
                if (checkWalletBalance()) {
                    bookSlots("chat")
                } else {
                    promptUserToAddMoney()
                }
            }
        }

        // Book Slot button listener
        binding.callButton.setOnClickListener {
            if (this.isFreeUser) {
                bookSlots("voice")
            } else {
                if (checkWalletBalance()) {
                    bookSlots("voice")
                } else {
                    promptUserToAddMoney()
                }
            }
        }

        binding.clWallet.setOnClickListener {
            val intent = Intent(this, WalletActivity::class.java)
            startActivity(intent)
            finish()
        }
        fetchAstrologerStatus()
    }

    private fun showTypeUI(isBusy: Int) {
        val gridLayoutManagerMinutes = GridLayoutManager(this, 3) // 3 columns
        binding.rvType.layoutManager = gridLayoutManagerMinutes
        val typeList = if (isBusy == 1)
            listOf("Call")
        else
            listOf("Chat", "Call")
        val adapter = TypeAdapter(typeList, object : TypeAdapter.OnTypeSelectedListener {
            override fun onTypeSelected(type: String) {
                this@BookSlotActivity.type = type.lowercase()
                resetUI()
                updateWalletBalanceUI()
            }
        })
        adapter.setSelectedType(type)
        binding.rvType.adapter = adapter
    }

    private fun resetUI() {
        binding.tvSelectMinutes.visibility = View.GONE
        binding.rvMinutes.visibility = View.GONE
        binding.timeSlotLayout.visibility = View.GONE
        binding.callButton.visibility = View.GONE
        binding.chatButton.visibility = View.GONE
    }

    private fun showMinutesUI() {
        binding.rvMinutes.visibility = View.VISIBLE
        binding.tvSelectMinutes.visibility = View.VISIBLE
        val gridLayoutManagerMinutes = GridLayoutManager(this, 3) // 3 columns
        binding.rvMinutes.layoutManager = gridLayoutManagerMinutes

        val minutesList: List<Int> = if (this@BookSlotActivity.isFreeUser)
            listOf(2, 5, 10, 15, 20, 25, 30) // Add more minutes as needed
        else
            listOf(5, 10, 15, 20, 25, 30) // Add more minutes as needed


        val listener = object : MinutesAdapter.OnMinuteSelectedListener {
            override fun onMinuteSelected(minute: Int) {
                this@BookSlotActivity.isFreeUser = minute == 2
                selectedDuration = minute
                if (type == "chat") {
                    binding.chatButton.visibility = View.VISIBLE
                    binding.callButton.visibility = View.GONE
                    binding.timeSlotLayout.visibility = View.GONE
                } else {
                    binding.chatButton.visibility = View.GONE
                    if (isFreeUser) {
                        binding.callButton.visibility = View.VISIBLE
                        binding.timeSlotLayout.visibility = View.GONE
                    } else {
                        binding.callButton.visibility = View.GONE
                        binding.timeSlotLayout.visibility = View.VISIBLE
                        loadAvailableSlots()
                    }
                }
                binding.rvTimeSlots.visibility = View.GONE
                binding.tvSlotsTime.visibility = View.GONE
            }
        }

        val adapter = MinutesAdapter(minutesList, listener)
        binding.rvMinutes.adapter = adapter
    }

    private fun fetchAstrologerStatus() {
        lifecycleScope.launch(Dispatchers.IO) {
            val headers = mutableMapOf<String, String>()
            headers["Content-Type"] = "application/json"
            headers["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)
            OkHttpNetworkProvider.get(
                BuildConfig.BASE_URL + "astrologers/profile/busy_status/$astrologerPhoneNumber",
                headers,
                null,
                null,
                AstrologerStatusResponseModel::class.java,
                object : OkHttpNetworkProvider.NetworkListener<AstrologerStatusResponseModel> {
                    override fun onResponse(response: AstrologerStatusResponseModel?) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            if (response != null) {
                                if (response.error == false) {
                                    if (response.isOnline == 1)
                                        response.isBusy?.let { showTypeUI(it) }
                                    else {
                                        showTypeUI(1)
                                    }
                                }
                            }
                        }
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                        lifecycleScope.launch(Dispatchers.Main) {
                            Utils.hideProgress()
                            Toast.makeText(
                                this@BookSlotActivity,
                                error?.message ?: "Something went wrong!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                })
        }
    }


    private fun updateWalletBalanceUI() {
        Utils.showProgress(this@BookSlotActivity, "Please wait...")
        lifecycleScope.launch(Dispatchers.IO) {
            val headers = mutableMapOf<String, String>()
            headers["Content-Type"] = "application/json"
            headers["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)
            OkHttpNetworkProvider.get(
                BuildConfig.BASE_URL + "wallet/get_balance/" + KeyStorePref.getString(
                    AppConstants.KEY_STORE_USER_ID
                ),
                headers,
                null,
                null,
                WalletResponseModel::class.java,
                object : OkHttpNetworkProvider.NetworkListener<WalletResponseModel> {
                    override fun onResponse(response: WalletResponseModel?) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Utils.hideProgress()
                        }
                        if (response != null) {
                            if (!response.walletBalance?.balance.isNullOrEmpty()) {
                                this@BookSlotActivity.walletBalance =
                                    response.walletBalance?.balance?.toDouble()!!
                                if (response.walletBalance.isFree == "1")
                                    this@BookSlotActivity.isFreeUser = true
                                // Update wallet balance text view
                                lifecycleScope.launch(Dispatchers.Main) {
                                    binding.tvWalletBalance.text =
                                        buildString {
                                            append("Rs. ")
                                            append(response.walletBalance.balance)
                                        }
                                    showMinutesUI()
                                }
                            }
                        }
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                        lifecycleScope.launch(Dispatchers.Main) {
                            Utils.hideProgress()
                            Toast.makeText(
                                this@BookSlotActivity,
                                error?.message ?: "Something went wrong!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
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
            OkHttpNetworkProvider.get(
                BuildConfig.BASE_URL + "astrologer/all_slots/$astrologerPhoneNumber",
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

            OkHttpNetworkProvider.post(
                BuildConfig.BASE_URL + "astrologer/astrologers_bookings",
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

        // Get the current time in "HH:mm" format
        val calendar = Calendar.getInstance()
        val currentTime = String.format(
            "%02d:%02d",
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )

        val availableTimeSlots = generateTimeSlots(
            slotsItem.startTime!!,
            slotsItem.endTime!!,
            intervalMinutes,
            selectedSlot ?: emptyList() // Pass an empty list if selectedSlot is null
        ).filter { slot ->
            // Only include slots with start time after the current time
            slot.startTime >= currentTime // Ensure slot start time is after current time
        }

        // Set up the TimeSlotsAdapter
        val timeSlotsAdapter = TimeSlotsAdapter(availableTimeSlots) { selectedTimeSlot ->

            this.selectedStartTime = "${selectedTimeSlot.startTime}:00"
            this.slotId = slotsItem.slotId!!
            Log.i(TAG, "setupTimeSlotsAdapter: ${selectedTimeSlot.startTime}:00")
            // Update UI with selected time frame
            binding.tvSlotsTime.visibility = View.VISIBLE
            if (type == "chat")
                binding.chatButton.visibility = View.VISIBLE
            else
                binding.callButton.visibility = View.VISIBLE
        }

        val gridLayoutManagerMinutes = GridLayoutManager(this, 4) // 3 columns
        binding.rvTimeSlots.layoutManager = gridLayoutManagerMinutes
        binding.rvTimeSlots.adapter = timeSlotsAdapter
    }


    private fun bookSlots(type: String) {
        if (this@BookSlotActivity.astrologerPhoneNumber.isEmpty()) {
            return
        }

        if (this@BookSlotActivity.slotId.isEmpty() && !isFreeUser && type != "chat") {
            return
        }

        if (this@BookSlotActivity.selectedDuration < 0) {
            Toast.makeText(
                this@BookSlotActivity,
                "Please select the minutes",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (this@BookSlotActivity.selectedStartTime.isEmpty() && !isFreeUser && type != "chat") {
            Toast.makeText(this@BookSlotActivity, "Please select a time", Toast.LENGTH_SHORT)
                .show()
            return
        }

        Utils.showProgress(this@BookSlotActivity, "Please wait...")
        lifecycleScope.launch(Dispatchers.IO) {
            val headerMap = mutableMapOf<String, String>()
            headerMap["Content-Type"] = "application/json"
            headerMap["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)

            val jsonObjectBody = JSONObject()
            jsonObjectBody.put("type", type)
            jsonObjectBody.put("astrologer_phone", this@BookSlotActivity.astrologerPhoneNumber)
            jsonObjectBody.put("user_phone", KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID))
            jsonObjectBody.put("call_duration_minutes", this@BookSlotActivity.selectedDuration)
            jsonObjectBody.put(
                "total_cost",
                (this@BookSlotActivity.selectedDuration * this@BookSlotActivity.astrologerRate)
            )
            if (type == "chat") {
                jsonObjectBody.put("booked_start_time", Utils.getCurrentTime())
            } else {
                if (!isFreeUser) {
                    jsonObjectBody.put("booked_start_time", this@BookSlotActivity.selectedStartTime)
                    jsonObjectBody.put("slot_id", this@BookSlotActivity.slotId)
                }
            }
            if (this@BookSlotActivity.isFreeUser)
                jsonObjectBody.put("is_free", isFreeUser)


            OkHttpNetworkProvider.post(
                BuildConfig.BASE_URL_V2 + "booking/book_slot",
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
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (type == "chat") {
                                    getChatToken()
                                } else
                                    showDialog()
                            }
                        }
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                        lifecycleScope.launch(Dispatchers.Main) {
                            Utils.hideProgress()
                            Toast.makeText(
                                this@BookSlotActivity,
                                error?.message ?: "Something went wrong",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }
                })
        }
    }

    private suspend fun getChatToken() {
        lifecycleScope.launch(Dispatchers.IO) {
            val headers = mutableMapOf<String, String>()
            headers["Content-Type"] = "application/x-www-form-urlencoded"
            headers["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)

            val jsonObjectBody = JSONObject()
            jsonObjectBody.put("phone", KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID))

            OkHttpNetworkProvider.post(
                BuildConfig.BASE_URL + "common/chat",
                jsonObjectBody,
                headers,
                null,
                null,
                ChatTokenResponseModel::class.java,
                object : OkHttpNetworkProvider.NetworkListener<ChatTokenResponseModel> {
                    override fun onResponse(response: ChatTokenResponseModel?) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Utils.hideProgress()
                            if (response != null) {
                                Log.i(TAG, "onResponse: $response")
                                if (response.error == false && !response.chatToken.isNullOrEmpty()) {
                                    connectToChatClient(astrologerPhoneNumber, response.chatToken)
                                } else {
                                    Toast.makeText(
                                        this@BookSlotActivity,
                                        response.message ?: "Something went wrong!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                        lifecycleScope.launch(Dispatchers.Main) {
                            Utils.hideProgress()
                            Toast.makeText(
                                this@BookSlotActivity,
                                error?.message ?: "Something went wrong!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                })
        }
    }

    private suspend fun connectToChatClient(astrologerPhone: String, token: String) {
        // Step 1: Connect to the chat client
        withContext(Dispatchers.IO) {
            SceytChatUIKit.connect(token)
        }

        // Step 2: Wait for connection result and update profile
        val connectionResult = withContext(Dispatchers.IO) {
            ConnectionEventManager.awaitToConnectSceyt()
        }

        if (connectionResult) {
            withContext(Dispatchers.Main) {
                SceytChatUIKit.chatUIFacade.userInteractor.updateProfile(
                    username = "",
                    firstName = KeyStorePref.getString(AppConstants.KEY_STORE_NAME),
                    lastName = "",
                    avatarUrl = null, // Pass your avatar URL here
                    metadataMap = null // Pass metadata if needed
                )
            }

            // Step 3: Create or find the pending channel and navigate to the chat activity
            val response = withContext(Dispatchers.IO) {
                SceytChatUIKit.chatUIFacade.channelInteractor.findOrCreatePendingChannelByMembers(
                    CreateChannelData(
                        type = "direct",
                        members = listOf(
                            SceytMember(
                                SceytUser(astrologerPhone),
                                "owner"
                            )
                        )
                    )
                )
            }

            if (response is SceytResponse.Success) {
                response.data?.let { sceytChannel ->
                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@BookSlotActivity, ChatActivity::class.java).apply {
                            putExtra("CHANNEL", sceytChannel)
                            putExtra("astrologer_phone", astrologerPhone)
                        }
                        startActivity(intent)
                    }
                }
            }
        } else {
            // Handle connection failure if needed
            println("Failed to connect to the chat client.")
        }
    }

    private fun showDialog() {
        // Create a new dialog
        val dialog = Dialog(this)

        // Hide the default title
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Set custom layout for the dialog
        dialog.setContentView(R.layout.dialog_book_slots)

        // Set dialog properties
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        // Set dialog background to transparent for corner radius to be visible
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);

        // Find views from the custom layout
        val dialogButton: Button = dialog.findViewById(R.id.btnOkay)
        val tvSuccess: TextView = dialog.findViewById(R.id.tvSuccess)

        if (isFreeUser) {
            tvSuccess.text = "You will receive a call back from us shortly!"
        } else {
            tvSuccess.text = "Booking successful. \nYou will receive a call from our astrologer"
        }

        // Set click listener for dialog button
        dialogButton.setOnClickListener { // Do something with the input text, if needed
            dialog.dismiss()
            val intent = Intent(this@BookSlotActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Show the dialog
        dialog.show()
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

        while (slotCount < maxSlots) {
            val nextTime = currentTime.plusMinutes(intervalMinutes.toLong())

            // Break if the nextTime crosses into the next day
            if (nextTime.isBefore(currentTime) || nextTime.isAfter(finalTime)) break

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
