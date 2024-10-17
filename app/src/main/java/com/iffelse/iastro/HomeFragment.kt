package com.iffelse.iastro

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.iffelse.iastro.databinding.FragmentHomeBinding
import com.iffelse.iastro.model.Astrologer
import com.iffelse.iastro.model.Availability
import com.iffelse.iastro.model.ProfileData
import com.iffelse.iastro.model.TimeSlot

@Suppress("NAME_SHADOWING")
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

        // Initialize Firebase Storage reference
        val storageReferenceGif = FirebaseStorage.getInstance().reference.child("homepage.gif")
        val storageReference = FirebaseStorage.getInstance().reference.child("homepage.png")

        // Get the URL for the image
        storageReference.downloadUrl.addOnSuccessListener { uri ->
            // Use Glide to load the image into an ImageView
            Glide.with(context)
                .load(uri)
                .placeholder(R.drawable.astrology_background)
                .into(binding.imageView) // Replace 'binding.imageView' with your ImageView ID
        }.addOnFailureListener { exception ->
            // Handle any errors
            Log.e("Firebase", "Error loading image: ", exception)
            // Get the URL for the image
            storageReferenceGif.downloadUrl.addOnSuccessListener { uri ->
                // Use Glide to load the image into an ImageView
                Glide.with(context)
                    .load(uri)
                    .placeholder(R.drawable.astrology_background)
                    .into(binding.imageView) // Replace 'binding.imageView' with your ImageView ID
            }.addOnFailureListener { exception ->
                // Handle any errors
                Log.e("Firebase", "Error loading image: ", exception)
            }
        }


        binding.titleConsultNow.setOnClickListener {
            listener?.onCardClick("call")
        }

        binding.includeKundli.cardView1.setOnClickListener {
            listener?.onCardClick("trending")  // Call the method in the activity
        }


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

                    if (astrologerList.size == 0)
                        binding.labelOurAstrologer.visibility = View.GONE
                    else
                        binding.labelOurAstrologer.visibility = View.VISIBLE

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
}
