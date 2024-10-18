package com.iffelse.iastro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.iffelse.iastro.databinding.ItemHistoryBinding
import com.iffelse.iastro.model.FormSubmission
import com.iffelse.iastro.utils.Utils

class BookingHistoryAdapter(private val bookingList: List<FormSubmission>) :
    RecyclerView.Adapter<BookingHistoryAdapter.BookingViewHolder>() {

    private lateinit var binding: ItemHistoryBinding


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_history,
            parent,
            false
        )
        return BookingViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookingList[position]
        holder.bind(booking)
    }

    override fun getItemCount(): Int = bookingList.size

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(booking: FormSubmission) {
            if (booking.astrologerName.isNullOrEmpty()) {
                binding.tvAstrologerName.visibility = View.GONE
            } else {
                binding.tvAstrologerName.visibility = View.VISIBLE
                binding.tvAstrologerName.text = booking.astrologerName
            }

            if (booking.message.isNullOrEmpty()) {
                binding.tvUserMessage.visibility = View.GONE
            } else {
                binding.tvUserMessage.visibility = View.VISIBLE
                binding.tvUserMessage.text = "Message: ${booking.message}"
            }

            if (booking.timestamp.isNullOrEmpty()) {
                binding.tvTimestamp.visibility = View.GONE
            } else {
                binding.tvTimestamp.visibility = View.VISIBLE
                binding.tvTimestamp.text =
                    Utils.convertTimestamp(booking.timestamp)
            }

            if (booking.timeToCall.isNullOrEmpty()) {
                binding.tvTimeSlot.visibility = View.GONE
            } else {
                binding.tvTimeSlot.visibility = View.VISIBLE
                binding.tvTimeSlot.text = "Time slot: ${booking.timeToCall}"
            }
        }
    }
}
