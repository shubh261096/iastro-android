package com.iffelse.iastro

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.iffelse.iastro.databinding.ItemAstrologerBinding
import com.iffelse.iastro.model.Astrologer

class AstrologerAdapter(private val astrologers: List<Astrologer>, private val context: Context, val cLickListener: CLickListener) :
    RecyclerView.Adapter<AstrologerAdapter.AstrologerViewHolder>() {

    private lateinit var binding: ItemAstrologerBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AstrologerViewHolder {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_astrologer,
            parent,
            false
        )
        return AstrologerViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: AstrologerViewHolder, position: Int) {
        val astrologer = astrologers[position]
        holder.bind(astrologer)

        // Adding a simple fade-in animation
        holder.itemView.alpha = 0f
        holder.itemView.animate().alpha(1f).setDuration(500).start()
    }

    override fun getItemCount(): Int = astrologers.size

    inner class AstrologerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(astrologer: Astrologer) {
            binding.astrologerName.text =  astrologer.name
            binding.astrologerSpeciality.text = astrologer.specialty
            binding.astrologerRating.text = astrologer.rating.toString()
            binding.astrologerReviews.text = "(${astrologer.reviews} reviews)"
            binding.astrologerDescription.text = astrologer.description
            // Convert image resource name to drawable resource ID and set it
            val imageResId = context.resources.getIdentifier(astrologer.photo, "drawable", context.packageName)
            binding.astrologerPhoto.setImageResource(imageResId)

            binding.executePendingBindings()

            // Call button click listener (can add functionality later)
            binding.callIcon.setOnClickListener {
                // Handle the call action here
                cLickListener.onClick()
            }
        }
    }

    interface CLickListener {
        fun onClick()
    }
}
