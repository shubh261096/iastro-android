package com.iffelse.iastro.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iffelse.iastro.R
import com.iffelse.iastro.databinding.ItemAstrologerBinding
import com.iffelse.iastro.model.response.DataItem

class AstrologerAdapter(
    private val astrologers: List<DataItem?>,
    private val context: Context,
    val astrologerAdapterClickListener: AstrologerAdapterClickListener
) :
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
        val astrologer = astrologers.get(position)
        holder.bind(astrologer!!)

        // Adding a simple fade-in animation
        holder.itemView.alpha = 0f
        holder.itemView.animate().alpha(1f).setDuration(500).start()
    }

    override fun getItemCount(): Int = astrologers.size

    inner class AstrologerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(astrologer: DataItem) {
            binding.astrologerName.text = astrologer.name
            binding.astrologerSpeciality.text = astrologer.specialty
            binding.astrologerRating.text = astrologer.ratings
            binding.astrologerReviews.text = buildString {
                append("(")
                append(astrologer.reviewsCount)
                append(" reviews)")
            }
            binding.astrologerDescription.text = astrologer.descriptionShort
            binding.astrologerRate.text = buildString {
                append("₹ ")
                append(astrologer.finalRate)
                append("/min")
            }

            if (astrologer.isActive == "1") {
                if (astrologer.isOnline != null && astrologer.isOnline == "1") {
                    if (astrologer.isBusy != null && astrologer.isBusy == "1") {
                        binding.chatIcon.visibility = View.GONE
                        binding.onlineStatus.setBackgroundResource(R.drawable.busy_dot)
                    } else {
                        binding.chatIcon.visibility = View.VISIBLE
                        binding.onlineStatus.setBackgroundResource(R.drawable.online_dot)
                    }
                    binding.onlineStatus.visibility = View.VISIBLE
                } else {
                    binding.chatIcon.visibility = View.GONE
                    binding.onlineStatus.visibility = View.GONE
                }
            } else {
                binding.chatIcon.visibility = View.GONE
                binding.onlineStatus.visibility = View.GONE
            }

            // Use Glide to load the actual image into the ImageView
            Glide.with(itemView.context)
                .load(astrologer.photo)
                .error(R.drawable.touch) // Set an error image if loading fails
                .into(binding.astrologerPhoto)

            binding.executePendingBindings()

            binding.chatIcon.setOnClickListener{
                astrologerAdapterClickListener.onChatClick(bindingAdapterPosition)
            }

            binding.callIcon.setOnClickListener{
                astrologerAdapterClickListener.onCallClick(bindingAdapterPosition)
            }
        }

    }

    interface CLickListener {
        fun onClick(position: Int)
    }

    interface AstrologerAdapterClickListener {
        fun onChatClick(position: Int)

        fun onCallClick(position: Int)
    }
}
