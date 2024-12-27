package com.iffelse.iastro.view.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.iffelse.iastro.BuildConfig
import com.iffelse.iastro.databinding.FragmentCallBinding
import com.iffelse.iastro.model.Banner
import com.iffelse.iastro.model.BaseErrorModel
import com.iffelse.iastro.model.response.AstrologerResponseModel
import com.iffelse.iastro.utils.AppConstants
import com.iffelse.iastro.utils.KeyStorePref
import com.iffelse.iastro.utils.OkHttpNetworkProvider
import com.iffelse.iastro.utils.Utils
import com.iffelse.iastro.view.adapter.AstrologerAdapter
import com.iffelse.iastro.view.adapter.BannerAdapter
import com.iffelse.iastro.view.ui.BookSlotActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallFragment : Fragment() {

    private lateinit var binding: FragmentCallBinding
    private lateinit var astrologerAdapter: AstrologerAdapter

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var currentPage = 0

    private val bannerList = listOf(
        Banner("Job related issues?", ""),
        Banner("Career advice needed?", ""),
        Banner("Relationship or Marriage Issues", ""),
        Banner("Work stress? Or Family troubles?", ""),
        Banner("When will my ex come back?", ""),
        Banner("When will I get married?", ""),
        Banner("What is my lucky number?", "")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize binding
        binding = FragmentCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Setup ViewPager for Banner
        val bannerAdapter = BannerAdapter(bannerList, object : AstrologerAdapter.CLickListener {
            override fun onClick(position: Int) {
                // TODO: Think of something
            }
        })
        binding.bannerViewpager.adapter = bannerAdapter

        // Start the auto-scrolling feature
        startAutoScroll()


        binding.recyclerViewAstrologers.layoutManager = LinearLayoutManager(requireActivity())

        // Create a list to hold the astrologer data

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
                        lifecycleScope.launch(Dispatchers.Main) {
                            if (isAdded && !isDetached && response != null) {
                                if (response.error == false) {
                                    if (response.data != null) {

                                        val activity = requireActivity()
                                        astrologerAdapter =
                                            AstrologerAdapter(
                                                response.data,
                                                requireActivity(),
                                                object : AstrologerAdapter.AstrologerAdapterClickListener {
                                                    override fun onChatClick(position: Int) {
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
                                                        intent.putExtra("type", "chat")
                                                        startActivity(intent)
                                                    }

                                                    override fun onCallClick(position: Int) {
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
                                                        intent.putExtra("type", "call")
                                                        startActivity(intent)
                                                    }
                                                })

                                    }

                                    binding.recyclerViewAstrologers.adapter = astrologerAdapter
                                }
                            }
                        }
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(activity, error?.message ?: "Something went wrong!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                })
        }
    }

    private fun startAutoScroll() {
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            currentPage = binding.bannerViewpager.currentItem
            val nextPage = (currentPage + 1) % bannerList.size // Loop back to the first item
            binding.bannerViewpager.setCurrentItem(nextPage, true)
            handler.postDelayed(
                runnable,
                2000
            ) // Adjust time interval as needed (e.g., 3000ms = 3s)
        }
        handler.postDelayed(runnable, 2000) // Start the auto-scroll after 3 seconds
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable) // Stop auto-scrolling when the view is destroyed
    }

    companion object {
        private const val TAG = "CallFragment"
    }
}
