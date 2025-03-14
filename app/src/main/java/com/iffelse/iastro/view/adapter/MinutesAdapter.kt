package com.iffelse.iastro.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iffelse.iastro.R


class MinutesAdapter(
    private val freeMinDuration : Int,
    private val minutesList: List<Int>,
    private val listener: OnMinuteSelectedListener
) : RecyclerView.Adapter<MinutesAdapter.MinuteViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MinuteViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return MinuteViewHolder(view)
    }

    override fun onBindViewHolder(holder: MinuteViewHolder, position: Int) {
        val minute = minutesList[position]
        holder.bind(minute, position == selectedPosition)

        holder.itemView.setOnClickListener {
            val previousSelectedPosition = selectedPosition
            selectedPosition = position

            notifyItemChanged(previousSelectedPosition)
            notifyItemChanged(selectedPosition)
            listener.onMinuteSelected(minute)
        }
    }

    override fun getItemCount(): Int = minutesList.size

    inner class MinuteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTimeSlot: TextView = itemView.findViewById(R.id.tvTimeSlot)

        fun bind(timeSlot: Int, isSelected: Boolean) {
            if (timeSlot == freeMinDuration) {
                tvTimeSlot.text = "FREE"
            } else {
                tvTimeSlot.text = "$timeSlot mins"
            }

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

    interface OnMinuteSelectedListener {
        fun onMinuteSelected(minute: Int)
    }
}


