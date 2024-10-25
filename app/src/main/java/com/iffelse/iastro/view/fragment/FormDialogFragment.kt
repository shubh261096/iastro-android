package com.iffelse.iastro.view.fragment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.GenericTypeIndicator
import com.iffelse.iastro.utils.KeyStorePref
import com.iffelse.iastro.view.adapter.TimeSlotAdapter
import com.iffelse.iastro.databinding.DialogFormBinding
import com.iffelse.iastro.model.Astrologer
import com.iffelse.iastro.model.Banner
import com.iffelse.iastro.model.BaseErrorModel
import com.iffelse.iastro.utils.FirebaseHelper
import com.iffelse.iastro.utils.FormData
import com.iffelse.iastro.utils.OkHttpNetworkProvider
import com.iffelse.iastro.utils.Utils
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class FormDialogFragment(
    private val context: Context,
    private val astrologer: Astrologer?,
    private val banner: Banner?
) :
    DialogFragment() {

    private lateinit var dialogFormBinding: DialogFormBinding
    private var selectedTimeSlot: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Inflate the form layout
        dialogFormBinding = DialogFormBinding.inflate(inflater)
        builder.setView(dialogFormBinding.root)

        val availableTimeSlots = mutableListOf<String>()

        // Load available time slots dynamically based on interval and time range
        // Check if astrologer or its availability is null
        astrologer?.availability?.timeSlots?.forEach { timeSlot ->
            val startTime = timeSlot.startTime ?: return@forEach
            val endTime = timeSlot.endTime ?: return@forEach
            val intervalMinutes = timeSlot.interval ?: 0

            if (intervalMinutes > 0) {
                availableTimeSlots.addAll(
                    generateTimeSlots(
                        startTime = startTime,
                        endTime = endTime,
                        intervalMinutes = intervalMinutes
                    )
                )
            }
        }

        if (availableTimeSlots.size == 0) {
            dialogFormBinding.tvTimeSlotTitle.visibility = View.GONE
        }


        // Use GridLayoutManager to show 3-4 items per row
        val spanCount =
            3 // Number of items per row (you can also adjust this dynamically based on screen size)

        val gridLayoutManager = GridLayoutManager(context, spanCount)
        dialogFormBinding.rvTimeSlots.apply {
            layoutManager = gridLayoutManager
            adapter = TimeSlotAdapter(availableTimeSlots) { selectedSlot ->
                selectedTimeSlot = selectedSlot
            }
        }

        banner?.message.let {
            dialogFormBinding.etMessage.setText(it)
        }

        val firebaseHelper = FirebaseHelper()
        val jsonObject = JSONObject()

        firebaseHelper.checkIfNameExists(KeyStorePref.getString("userId")!!) { hasName, dataSnapShot ->
            if (hasName) {
                val name = dataSnapShot!!.child("name").getValue(String::class.java)
                val dob = dataSnapShot.child("dob").getValue(String::class.java)
                val gender = dataSnapShot.child("gender").getValue(String::class.java)
                val placeOfBirth = dataSnapShot.child("placeOfBirth").getValue(String::class.java)
                val timeOfBirth = dataSnapShot.child("time").getValue(String::class.java)
                val preferredLanguages = dataSnapShot.child("languages")
                    .getValue(object : GenericTypeIndicator<List<String>>() {})

                jsonObject.put("dob", dob)
                jsonObject.put("gender", gender)
                jsonObject.put("placeOfBirth", placeOfBirth)
                jsonObject.put("time", timeOfBirth)

                // You can now use preferredLanguages as a list, if not null
                if (preferredLanguages != null) {
                    val languagesString = preferredLanguages.joinToString(", ")
                    jsonObject.put("languages", languagesString)
                }

                dialogFormBinding.etName.setText(name)
            }
        }
        // Submit button logic
        dialogFormBinding.btnSubmit.setOnClickListener {
            if (astrologer != null) {
                if (selectedTimeSlot == null) {
                    Toast.makeText(context, "Please select a time slot", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            Utils.closeKeyboard(context, it)

            val formData = FormData(
                name = dialogFormBinding.etName.text.toString().trim(),
                astrologerName = astrologer?.profileData?.name ?: "",
                message = dialogFormBinding.etMessage.text.toString().trim(),
                timeToCall = selectedTimeSlot ?: "" // Use the selected time slot
            )

            val url = "https://www.apsdeoria.com/apszone/api/v2/qa/test/vendor/sendEmail"

            jsonObject.put("name", dialogFormBinding.etName.text.toString().trim())
            jsonObject.put("phoneNumber", KeyStorePref.getString("userId"))
            jsonObject.put("astrologerName", formData.astrologerName)
            jsonObject.put("message", dialogFormBinding.etMessage.text.toString().trim())
            jsonObject.put("timeToCall", selectedTimeSlot ?: "")

            OkHttpNetworkProvider.post(
                url,
                jsonObject,
                mutableMapOf(),
                null,
                null,
                responseType = JSONObject::class.java,
                object : OkHttpNetworkProvider.NetworkListener<JSONObject> {
                    override fun onResponse(response: JSONObject?) {
                        Log.i("TAG", "onResponse: ")
                    }

                    override fun onError(error: BaseErrorModel?) {

                    }
                }
            )

            firebaseHelper.saveFormSubmission(KeyStorePref.getString("userId")!!, formData)
            showSuccessMessage()
        }

        return builder.create()
    }

    // Function to generate time slots based on start time, end time, and interval
    private fun generateTimeSlots(
        startTime: String,
        endTime: String,
        intervalMinutes: Int
    ): List<String> {
        val timeSlots = mutableListOf<String>()
        val sdf24 = SimpleDateFormat("HH:mm", Locale.getDefault()) // 24-hour format
        val sdf12 = SimpleDateFormat("hh:mm a", Locale.getDefault()) // 12-hour format with AM/PM

        try {
            val start = sdf24.parse(startTime)
            val end = sdf24.parse(endTime)

            if (start != null && end != null) {
                var currentTime = start.time
                while (currentTime <= end.time) {
                    val timeString = sdf12.format(Date(currentTime))
                    timeSlots.add(timeString)
                    currentTime += TimeUnit.MINUTES.toMillis(intervalMinutes.toLong()) // Increment by interval
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return timeSlots
    }

    private fun showSuccessMessage() {
        dialogFormBinding.clSuccess.visibility = View.VISIBLE
        dialogFormBinding.clForm.visibility = View.GONE

        Handler(Looper.getMainLooper()).postDelayed({
            dismiss()
        }, 3000)
    }
}
