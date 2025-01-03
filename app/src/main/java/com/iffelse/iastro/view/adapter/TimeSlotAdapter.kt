package com.iffelse.iastro.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iffelse.iastro.R

class TimeSlotAdapter(
    private val timeSlots: List<String>,
    private val onSlotSelected: (String) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val timeSlot = timeSlots[position]
        holder.bind(timeSlot, position == selectedPosition)

        holder.itemView.setOnClickListener {
            val previousSelectedPosition = selectedPosition
            selectedPosition = position

            notifyItemChanged(previousSelectedPosition)
            notifyItemChanged(selectedPosition)

            onSlotSelected(timeSlot)
        }
    }

    override fun getItemCount(): Int = timeSlots.size

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTimeSlot: TextView = itemView.findViewById(R.id.tvTimeSlot)

        fun bind(timeSlot: String, isSelected: Boolean) {
            tvTimeSlot.text = timeSlot

            if (isSelected) {
                // Selected state - orange background with white text
                itemView.setBackgroundResource(R.drawable.slot_background_selected)
                tvTimeSlot.setTextColor(Color.WHITE)
            } else {
                // Unselected state - default background with black text
                itemView.setBackgroundResource(R.drawable.slot_background)
                tvTimeSlot.setTextColor(Color.BLACK)
            }
        }
    }
}