package com.iffelse.iastro.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iffelse.iastro.R
import com.iffelse.iastro.model.response.slots.AllSlotsItem
import com.iffelse.iastro.utils.Utils

class SlotAdapter(private val onSlotSelected: (AllSlotsItem) -> Unit) :
    RecyclerView.Adapter<SlotAdapter.SlotViewHolder>() {

    private val slots: MutableList<AllSlotsItem> = mutableListOf()
    private var selectedPosition = -1

    fun updateSlots(newSlots: List<AllSlotsItem?>?) {
        slots.clear()
        // Check if newSlots is not null, and filter out any null items before adding them to the list
        if (newSlots != null) {
            slots.addAll(newSlots.filterNotNull())
        }
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val slot = slots[position]
        holder.bind(slot, position == selectedPosition)

        holder.itemView.setOnClickListener {
            val previousSelectedPosition = selectedPosition
            selectedPosition = position

            notifyItemChanged(previousSelectedPosition)
            notifyItemChanged(selectedPosition)
            onSlotSelected(slot)
        }
    }

    override fun getItemCount(): Int {
        return slots.size
    }

    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val startTimeText: TextView = itemView.findViewById(R.id.tvTimeSlot)

        fun bind(slot: AllSlotsItem, isSelected: Boolean) {

            if (isSelected) {
                // Selected state - orange background with white text
                itemView.setBackgroundResource(R.drawable.slot_background_selected)
                startTimeText.setTextColor(Color.WHITE)
            } else {
                // Unselected state - default background with black text
                itemView.setBackgroundResource(R.drawable.slot_background)
                startTimeText.setTextColor(Color.BLACK)
            }
            startTimeText.text = buildString {
                append(Utils.convertTo12HourFormat(slot.startTime!!))
                append(" -\n")
                append(Utils.convertTo12HourFormat(slot.endTime!!))
            }
        }
    }
}
