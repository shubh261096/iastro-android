package com.iffelse.iastro

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.iffelse.iastro.databinding.FragmentTrendingBinding
import com.iffelse.iastro.model.BaseErrorModel
import com.iffelse.iastro.utils.OkHttpNetworkProvider
import com.iffelse.iastro.utils.Utils
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class TrendingFragment : Fragment() {

    private lateinit var binding: FragmentTrendingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTrendingBinding.inflate(inflater, container, false)
        setupListeners()

        // Initial state: Set "Today" button as active by default
        setButtonState(binding.btnToday)
        fetchHoroscope("today")

        return binding.root
    }

    private fun setupListeners() {
        binding.btnYesterday.setOnClickListener {
            fetchHoroscope("yesterday")
            setButtonState(binding.btnYesterday)
        }
        binding.btnToday.setOnClickListener {
            fetchHoroscope("today")
            setButtonState(binding.btnToday)
        }
        binding.btnTomorrow.setOnClickListener {
            fetchHoroscope("tomorrow")
            setButtonState(binding.btnTomorrow)
        }
    }

    // Function to change button states
    private fun setButtonState(activeButton: Button) {
        // Set all buttons to inactive state
        binding.btnYesterday.setBackgroundResource(R.drawable.inactive_button_background)
        binding.btnToday.setBackgroundResource(R.drawable.inactive_button_background)
        binding.btnTomorrow.setBackgroundResource(R.drawable.inactive_button_background)

        // Set the active button to active state
        activeButton.setBackgroundResource(R.drawable.active_button_background)

        // Optionally, change text colors as well if needed
        binding.btnYesterday.setTextColor(resources.getColor(R.color.orange))
        binding.btnToday.setTextColor(resources.getColor(R.color.orange))
        binding.btnTomorrow.setTextColor(resources.getColor(R.color.orange))

        activeButton.setTextColor(resources.getColor(R.color.white))
    }

    private fun fetchHoroscope(day: String) {

        val firebaseHelper = FirebaseHelper()

        firebaseHelper.checkIfNameExists(KeyStorePref.getString("userId")!!) { hasName, dataSnapShot ->
            if (hasName) {
                val dateOfBirth = dataSnapShot!!.child("dob").getValue(String::class.java)
                // Example Date of Birth
                val dob = dateOfBirth?.let {
                    SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                    ).parse(it)
                }

                val sign = dob?.let { Utils.getSunSign(it) }
                OkHttpNetworkProvider.get("https://horoscope-app-api.vercel.app/api/v1/get-horoscope/daily?sign=$sign&day=$day",
                    null,
                    null,
                    "",
                    responseType = JSONObject::class.java,
                    object : OkHttpNetworkProvider.NetworkListener<JSONObject> {
                        override fun onResponse(response: JSONObject?) {
                            Log.i("TAG", "onResponse: ${response.toString()}")
                            activity?.runOnUiThread {
                                updateHoroscopeUI(response, sign!!)
                            }
                        }

                        override fun onError(error: BaseErrorModel?) {

                        }
                    })
            }
        }

    }

    private fun updateHoroscopeUI(jsonResponse: JSONObject?, sunSign: String) {
        // Parse JSON response and update the text fields (assuming you have the fields in JSON)
        // For example:
        val horoscopeDetails = parseHoroscopeDetails(jsonResponse)

        binding.tvSignName.text = sunSign
        binding.tvHoroscopeDate.text = horoscopeDetails.date
        binding.tvHoroscopeDetails.text = horoscopeDetails.description
    }

    private fun parseHoroscopeDetails(json: JSONObject?): HoroscopeDetails {
        // Parse the JSON and return HoroscopeDetails object
        // This is a dummy implementation, you need to adjust it based on your actual API response
        val jsonObject = json!!.get("data") as JSONObject

        return HoroscopeDetails(
            description = jsonObject.getString("horoscope_data"),
            date = jsonObject.getString("date")
        )
    }
}

data class HoroscopeDetails(val signName: String = "", val description: String, val date: String)
