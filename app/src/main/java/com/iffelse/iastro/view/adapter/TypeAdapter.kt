package com.iffelse.iastro.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iffelse.iastro.R


class TypeAdapter(
    private val minutesList: List<String>,
    private val listener: OnTypeSelectedListener
) : RecyclerView.Adapter<TypeAdapter.MinuteViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MinuteViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return MinuteViewHolder(view)
    }

    override fun onBindViewHolder(holder: MinuteViewHolder, position: Int) {
        val type = minutesList[position]
        holder.bind(type, position == selectedPosition)

        holder.itemView.setOnClickListener {
            val previousSelectedPosition = selectedPosition
            selectedPosition = position

            notifyItemChanged(previousSelectedPosition)
            notifyItemChanged(selectedPosition)
            listener.onTypeSelected(type)
        }
    }

    fun setSelectedType(selectedType: String) {
        val position = minutesList.indexOfFirst { it.equals(selectedType, ignoreCase = true) }
        if (position != -1) {
            val previousSelectedPosition = selectedPosition
            selectedPosition = position

            notifyItemChanged(previousSelectedPosition)
            notifyItemChanged(selectedPosition)
            listener.onTypeSelected(selectedType)
        }
    }

    override fun getItemCount(): Int = minutesList.size

    inner class MinuteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTimeSlot: TextView = itemView.findViewById(R.id.tvTimeSlot)
        fun bind(type: String, isSelected: Boolean) {
            tvTimeSlot.text = type
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

    interface OnTypeSelectedListener {
        fun onTypeSelected(type: String)
    }
}


