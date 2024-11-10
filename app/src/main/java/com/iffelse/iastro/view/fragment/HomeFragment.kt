package com.iffelse.iastro.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.iffelse.iastro.BuildConfig
import com.iffelse.iastro.utils.KeyStorePref
import com.iffelse.iastro.R
import com.iffelse.iastro.view.adapter.AstrologerAdapter
import com.iffelse.iastro.databinding.FragmentHomeBinding
import com.iffelse.iastro.model.BaseErrorModel
import com.iffelse.iastro.model.response.AstrologerResponseModel
import com.iffelse.iastro.utils.AppConstants
import com.iffelse.iastro.utils.OkHttpNetworkProvider
import com.iffelse.iastro.utils.RemoteConfigUtils
import com.iffelse.iastro.utils.Utils
import com.iffelse.iastro.view.ui.BookSlotActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var astrologerAdapter: AstrologerAdapter
    private lateinit var context: Context

    // Define an interface
    interface OnCardClickListener {
        fun onCardClick(page: String)  // The activity will implement this
    }

    private var listener: OnCardClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
        if (context is OnCardClickListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnCardClickListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null  // Prevent memory leaks
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize binding
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch Homepage Background
        val configKey = "homepage_background_url"
        val configDefaultValue =
            "https://www.apsdeoria.com/iastro_web/dist/img/app_content/homepage.gif"

        // Fetch the data using the specified key
        RemoteConfigUtils.fetchData(configKey, configDefaultValue) { dataString ->
            // Use the fetched data string here
            println("Remote Config Fetched Value: $dataString")
            if (dataString.isNotEmpty()) {
                // Check if fragment is in a valid state before loading images
                if (isAdded && !isDetached) {
                    Glide.with(context)
                        .load(dataString)
                        .placeholder(R.drawable.astrology_background)
                        .into(binding.imageView) // Replace 'binding.imageView' with your ImageView ID
                }
            }
        }


        binding.titleConsultNow.setOnClickListener {
            listener?.onCardClick("call")
        }

        binding.includeKundli.cardView1.setOnClickListener {
            listener?.onCardClick("trending")  // Call the method in the activity
        }


        binding.recyclerViewAstrologers.layoutManager = LinearLayoutManager(requireActivity())


        lifecycleScope.launch(Dispatchers.IO) {
            val headers = mutableMapOf<String, String>()
            headers["Content-Type"] = "application/json"
            headers["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)
            OkHttpNetworkProvider.get(
                BuildConfig.BASE_URL + "astrologer",
                headers,
                null,
                null,
                AstrologerResponseModel::class.java,
                object : OkHttpNetworkProvider.NetworkListener<AstrologerResponseModel> {
                    override fun onResponse(response: AstrologerResponseModel?) {
                        if (isAdded && !isDetached && response != null) {
                            if (response.error == false) {
                                if (response.data != null) {
                                    if (response.data.isEmpty())
                                        binding.labelOurAstrologer.visibility = View.GONE
                                    else {
                                        binding.labelOurAstrologer.visibility = View.VISIBLE
                                        val activity = requireActivity()
                                        astrologerAdapter =
                                            AstrologerAdapter(
                                                response.data,
                                                requireActivity(),
                                                object : AstrologerAdapter.CLickListener {
                                                    override fun onClick(position: Int) {
                                                        val intent = Intent(
                                                            activity,
                                                            BookSlotActivity::class.java
                                                        )
                                                        intent.putExtra(
                                                            "astrologer_phone",
                                                            response.data[position]?.phoneNumber
                                                        )
                                                        intent.putExtra(
                                                            "final_rate",
                                                            response.data[position]?.finalRate
                                                        )
                                                        startActivity(intent)
                                                    }
                                                })

                                    }


                                    lifecycleScope.launch(Dispatchers.Main) {
                                        binding.recyclerViewAstrologers.adapter =
                                            astrologerAdapter
                                    }
                                }
                            }
                        }
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(activity, error?.message ?: "Something went wrong", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                })
        }
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}
