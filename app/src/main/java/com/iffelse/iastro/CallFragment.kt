package com.iffelse.iastro

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iffelse.iastro.databinding.FragmentCallBinding
import com.iffelse.iastro.model.Astrologer
import java.io.IOException

class CallFragment : Fragment() {

    private lateinit var binding: FragmentCallBinding
    private lateinit var astrologerAdapter: AstrologerAdapter

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var currentPage = 0

    private val bannerImages = listOf(
        "Job related issues?",
        "Career Advice needed?",
        "Relationship or Marriage Issues",
        "Work stress? Or Family troubles",
        "When will my ex come back?",
        "When will I get married?",
        "What is my lucky number?"
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
        val bannerAdapter = BannerAdapter(bannerImages, object : AstrologerAdapter.CLickListener {
            override fun onClick(position: Int) {
                val dialog = FormDialogFragment(activity!!, null)
                dialog.show(activity!!.supportFragmentManager, "FormDialogFragment")
            }
        })
        binding.bannerViewpager.adapter = bannerAdapter

        // Start the auto-scrolling feature
        startAutoScroll()


        binding.recyclerViewAstrologers.layoutManager = LinearLayoutManager(requireActivity())

        val astrologers: List<Astrologer> =
            loadAstrologersFromJson<Astrologer>(requireActivity(), "loading")

        astrologerAdapter =
            AstrologerAdapter(
                astrologers,
                requireActivity(),
                object : AstrologerAdapter.CLickListener {
                    override fun onClick(position: Int) {
                        val dialog = FormDialogFragment(activity!!, astrologers[position])
                        dialog.show(activity!!.supportFragmentManager, "FormDialogFragment")
                    }

                })
        binding.recyclerViewAstrologers.adapter = astrologerAdapter
    }

    private fun startAutoScroll() {
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            currentPage = binding.bannerViewpager.currentItem
            val nextPage = (currentPage + 1) % bannerImages.size // Loop back to the first item
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


    private inline fun <reified T> loadAstrologersFromJson(
        context: Context,
        filename: String
    ): List<T> {
        var json: String? = null
        try {
            val inputStream = context.assets.open("$filename.json") // Open file from assets folder
            json = inputStream.bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Ensure the correct type is passed
        return if (json != null) {
            val listType = object : TypeToken<List<T>>() {}.type
            Gson().fromJson(json, listType) ?: emptyList()
        } else {
            emptyList()
        }
    }
}