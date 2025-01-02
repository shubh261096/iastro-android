package com.iffelse.iastro.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.iffelse.iastro.R
import com.iffelse.iastro.databinding.ItemHistoryBinding
import com.iffelse.iastro.model.response.BookingsHistoryItem
import com.iffelse.iastro.utils.Utils
import com.sceyt.chatuikit.extensions.firstCharToUppercase

class BookingHistoryAdapter(private val bookingList: List<BookingsHistoryItem?>?) :
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
        val booking = bookingList?.get(position)
        booking?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int = bookingList!!.size

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(booking: BookingsHistoryItem) {
            if (booking.astrologerName.isNullOrEmpty()) {
                binding.tvAstrologerName.visibility = View.GONE
            } else {
                binding.tvAstrologerName.visibility = View.VISIBLE
                binding.tvAstrologerName.text = booking.astrologerName
            }

            if (booking.totalCost.isNullOrEmpty()) {
                binding.tvUserMessage.visibility = View.GONE
            } else {
                binding.tvUserMessage.visibility = View.VISIBLE
                binding.tvUserMessage.text = "Total Cost: ${booking.totalCost}"
            }

            if (booking.timestamp.isNullOrEmpty()) {
                binding.tvTimestamp.visibility = View.GONE
            } else {
                binding.tvTimestamp.visibility = View.VISIBLE
                binding.tvTimestamp.text =
                    Utils.convertTimestamp(booking.timestamp)
            }

            if (booking.bookedStartTime.isNullOrEmpty()) {
                binding.tvTimeSlot.visibility = View.GONE
            } else {
                binding.tvTimeSlot.visibility = View.VISIBLE
                binding.tvTimeSlot.text = "Booked slot time: ${booking.bookedStartTime}"
            }

            if (booking.type.isNullOrEmpty()) {
                binding.tvType.visibility = View.GONE
            } else {
                binding.tvType.visibility = View.VISIBLE
                binding.tvType.text = booking.type.firstCharToUppercase()
            }
        }
    }
}
