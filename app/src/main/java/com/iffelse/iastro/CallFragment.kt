package com.iffelse.iastro

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iffelse.iastro.databinding.FragmentCallBinding
import com.iffelse.iastro.model.Astrologer
import com.iffelse.iastro.model.Availability
import com.iffelse.iastro.model.Banner
import com.iffelse.iastro.model.ProfileData
import com.iffelse.iastro.model.TimeSlot
import java.io.IOException

class CallFragment : Fragment() {

    private lateinit var binding: FragmentCallBinding
    private lateinit var astrologerAdapter: AstrologerAdapter

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var currentPage = 0

    private val bannerList = listOf(
        Banner("Job related issues?", ""),
        Banner("Career Advice needed?", ""),
        Banner("Relationship or Marriage Issues", ""),
        Banner("Work stress? Or Family troubles", ""),
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
                val dialog = FormDialogFragment(activity!!, null, bannerList[position])
                dialog.show(activity!!.supportFragmentManager, "FormDialogFragment")
            }
        })
        binding.bannerViewpager.adapter = bannerAdapter

        // Start the auto-scrolling feature
        startAutoScroll()


        binding.recyclerViewAstrologers.layoutManager = LinearLayoutManager(requireActivity())

        // Create a list to hold the astrologer data
        val astrologerList = mutableListOf<Astrologer>()

        // Reference to the 'astrologers' node in Firebase Realtime Database
        val databaseReference = FirebaseDatabase.getInstance().getReference("astrologers")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Ensure the fragment is still attached before calling requireActivity()
                if (isAdded) {
                    // Now it is safe to call requireActivity() because the fragment is attached
                    val activity = requireActivity() // Safe to call
                    // Perform the rest of your operations here
                    astrologerList.clear() // Clear list to prevent duplicates

                    for (astrologerSnapshot in snapshot.children) {
                        // Fetch profileData
                        val profileDataSnapshot = astrologerSnapshot.child("profileData")
                        val name = profileDataSnapshot.child("name").getValue(String::class.java)
                        val specialty =
                            profileDataSnapshot.child("specialty").getValue(String::class.java)
                        val rating = profileDataSnapshot.child("rating").getValue(Float::class.java)
                        val reviews = profileDataSnapshot.child("reviews").getValue(Int::class.java)
                        val description =
                            profileDataSnapshot.child("description").getValue(String::class.java)
                        val photo = profileDataSnapshot.child("photo").getValue(String::class.java)
                        val rate = profileDataSnapshot.child("rate").getValue(String::class.java)
                        val isActive =
                            profileDataSnapshot.child("isActive").getValue(Boolean::class.java)
                        val isOnline =
                            profileDataSnapshot.child("isOnline").getValue(Boolean::class.java)


                        // Create ProfileData object
                        val profileData = ProfileData(
                            name = name,
                            specialty = specialty,
                            rating = rating,
                            reviews = reviews,
                            description = description,
                            photo = photo,
                            rate = rate,
                            isActive = isActive,
                            isOnline = isOnline
                        )

                        // Fetch availability data
                        val availabilitySnapshot = astrologerSnapshot.child("availability")
                        val timeSlotList = mutableListOf<TimeSlot>()
                        for (slotSnapshot in availabilitySnapshot.child("timeSlots").children) {
                            val startTime =
                                slotSnapshot.child("startTime").getValue(String::class.java)
                            val endTime = slotSnapshot.child("endTime").getValue(String::class.java)
                            val interval = slotSnapshot.child("interval").getValue(Int::class.java)
                            timeSlotList.add(TimeSlot(startTime, endTime, interval))
                        }

                        // Create Availability object
                        val availability = Availability(timeSlots = timeSlotList)

                        // Create Astrologer object
                        val astrologer = Astrologer(
                            profileData = profileData,
                            availability = availability
                        )

                        if (isActive == null || isActive == true) {
                            // Add to the list
                            astrologerList.add(astrologer)
                        }
                    }

                    astrologerAdapter =
                        AstrologerAdapter(
                            astrologerList,
                            requireActivity(),
                            object : AstrologerAdapter.CLickListener {
                                override fun onClick(position: Int) {
                                    val dialog =
                                        FormDialogFragment(activity, astrologerList[position], null)
                                    dialog.show(
                                        activity.supportFragmentManager,
                                        "FormDialogFragment"
                                    )
                                }

                            })
                    binding.recyclerViewAstrologers.adapter = astrologerAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching data", error.toException())
            }
        })
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
