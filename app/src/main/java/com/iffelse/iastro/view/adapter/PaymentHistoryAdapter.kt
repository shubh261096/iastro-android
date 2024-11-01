package com.iffelse.iastro.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.iffelse.iastro.R
import com.iffelse.iastro.databinding.ItemHistoryBinding
import com.iffelse.iastro.model.response.PaymentHistoryItem
import com.iffelse.iastro.utils.Utils

class PaymentHistoryAdapter(private val paymentList: List<PaymentHistoryItem?>?) :
    RecyclerView.Adapter<PaymentHistoryAdapter.BookingViewHolder>() {

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
        val booking = paymentList?.get(position)
        booking?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int = paymentList!!.size

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(paymentItem: PaymentHistoryItem) {
            if (paymentItem.transactionType.isNullOrEmpty()) {
                binding.tvAstrologerName.visibility = View.GONE
            } else {
                binding.tvAstrologerName.visibility = View.VISIBLE
                binding.tvAstrologerName.text = paymentItem.transactionType
            }

            if (paymentItem.amount.isNullOrEmpty()) {
                binding.tvUserMessage.visibility = View.GONE
            } else {
                binding.tvUserMessage.visibility = View.VISIBLE
                binding.tvUserMessage.text = "Amount: ${paymentItem.amount}"
            }

            if (paymentItem.transactionDate.isNullOrEmpty()) {
                binding.tvTimestamp.visibility = View.GONE
            } else {
                binding.tvTimestamp.visibility = View.VISIBLE
                binding.tvTimestamp.text =
                    Utils.convertTimestamp(paymentItem.transactionDate)
            }

            if (paymentItem.paymentGatewayTransactionId.isNullOrEmpty()) {
                binding.tvTimeSlot.visibility = View.GONE
            } else {
                binding.tvTimeSlot.visibility = View.VISIBLE
                binding.tvTimeSlot.text = "Order Id: ${paymentItem.paymentGatewayTransactionId}"
            }
        }
    }
}
