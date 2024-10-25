package com.iffelse.iastro

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.iffelse.iastro.model.response.slots.TimeSlot

class TimeSlotsAdapter(
    private val timeSlots: List<TimeSlot>,
    private val onTimeSlotSelected: (TimeSlot) -> Unit
) : RecyclerView.Adapter<TimeSlotsAdapter.TimeSlotViewHolder>() {

    private var selectedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val timeSlot = timeSlots[position]
        holder.bind(timeSlot, position == selectedPosition)
    }

    override fun getItemCount(): Int {
        return timeSlots.size
    }

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTimeSlot: TextView = itemView.findViewById(R.id.tvTimeSlot)

        fun bind(timeSlot: TimeSlot, isSelected: Boolean) {
            tvTimeSlot.text = timeSlot.displayTime
            tvTimeSlot.isEnabled = !timeSlot.isBooked

            // Change appearance based on selection or if the slot is booked
            if (isSelected) {
                itemView.setBackgroundResource(R.drawable.slot_background_selected)
                tvTimeSlot.setTextColor(Color.WHITE)
            } else if (timeSlot.isBooked) {
                itemView.setBackgroundResource(R.drawable.booked_slot_background)
                tvTimeSlot.setTextColor(Color.BLACK)
            } else {
                itemView.setBackgroundResource(R.drawable.available_slot_background)
                tvTimeSlot.setTextColor(Color.BLACK)
            }

            // Click listener for slot selection
            tvTimeSlot.setOnClickListener {
                if (!timeSlot.isBooked) {
                    selectedPosition = adapterPosition
                    notifyDataSetChanged()
                    onTimeSlotSelected(timeSlot)
                }
            }
        }
    }
}

