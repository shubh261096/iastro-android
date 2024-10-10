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
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iffelse.iastro.databinding.FragmentHomeBinding
import com.iffelse.iastro.model.Astrologer
import java.io.IOException
import java.text.FieldPosition

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var astrologerAdapter: AstrologerAdapter

    // Define an interface
    interface OnCardClickListener {
        fun onCardClick(page: String)  // The activity will implement this
    }

    private var listener: OnCardClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
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
            Glide.with(requireContext())
                .load(uri)
                .into(binding.imageView) // Replace 'binding.imageView' with your ImageView ID
        }.addOnFailureListener { exception ->
            // Handle any errors
            Log.e("Firebase", "Error loading image: ", exception)
            // Get the URL for the image
            storageReferenceGif.downloadUrl.addOnSuccessListener { uri ->
                // Use Glide to load the image into an ImageView
                Glide.with(requireContext())
                    .load(uri)
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
                for (astrologerSnapshot in snapshot.children) {
                    // Map each child node to Astrologer object
                    val astrologer =
                        astrologerSnapshot.child("profileData").getValue(Astrologer::class.java)
                    astrologer?.let {
                        astrologerList.add(it)
                    }
                }

                astrologerAdapter =
                    AstrologerAdapter(
                        astrologerList,
                        requireActivity(),
                        object : AstrologerAdapter.CLickListener {
                            override fun onClick(position: Int) {
                                val dialog = FormDialogFragment(activity!!, astrologerList[position])
                                dialog.show(activity!!.supportFragmentManager, "FormDialogFragment")
                            }

                        })
                binding.recyclerViewAstrologers.adapter = astrologerAdapter
                // Now astrologerList contains all the astrologer data
                // You can update the RecyclerView adapter or perform other operations with this list
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching data", error.toException())
            }
        })


    }

    override fun onDestroyView() {
        super.onDestroyView()
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
