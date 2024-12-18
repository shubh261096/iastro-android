package com.iffelse.iastro.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.iffelse.iastro.R
import com.iffelse.iastro.databinding.FragmentTrendingBinding
import com.iffelse.iastro.model.BaseErrorModel
import com.iffelse.iastro.utils.AppConstants
import com.iffelse.iastro.utils.KeyStorePref
import com.iffelse.iastro.utils.OkHttpNetworkProvider
import com.iffelse.iastro.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        binding.btnYesterday.setTextColor(ContextCompat.getColor(requireActivity(), R.color.orange))
        binding.btnToday.setTextColor(ContextCompat.getColor(requireActivity(), R.color.orange))
        binding.btnTomorrow.setTextColor(ContextCompat.getColor(requireActivity(), R.color.orange))

        activeButton.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
    }

    private fun fetchHoroscope(day: String) {
        if (!KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID).isNullOrEmpty() &&
            !KeyStorePref.getString(AppConstants.KEY_STORE_DOB).isNullOrEmpty()
        ) {
            val dateOfBirth = KeyStorePref.getString(AppConstants.KEY_STORE_DOB)
            // Example Date of Birth
            val dob = dateOfBirth?.let { Utils.parseDate(it) }


            val sign = dob?.let { Utils.getSunSign(it) }
            lifecycleScope.launch(Dispatchers.IO) {
                OkHttpNetworkProvider.get("https://horoscope-app-api.vercel.app/api/v1/get-horoscope/daily?sign=$sign&day=$day",
                    null,
                    null,
                    "",
                    responseType = JSONObject::class.java,
                    object : OkHttpNetworkProvider.NetworkListener<JSONObject> {
                        override fun onResponse(response: JSONObject?) {
                            Log.i("TAG", "onResponse: ${response.toString()}")
                            lifecycleScope.launch(Dispatchers.Main) {
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
