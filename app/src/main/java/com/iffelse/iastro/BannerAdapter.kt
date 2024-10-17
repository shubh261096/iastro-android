package com.iffelse.iastro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iffelse.iastro.model.Astrologer
import com.iffelse.iastro.model.Banner

class BannerAdapter(
    private val banner: List<Banner>,
    private val cLickListener: AstrologerAdapter.CLickListener
) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(banner[position])
    }

    override fun getItemCount(): Int = banner.size

    inner class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(banner: Banner) {
            itemView.findViewById<TextView>(R.id.banner_image).text = banner.message
            // Call button click listener (can add functionality later)
            itemView.setOnClickListener {
                cLickListener.onClick(adapterPosition)
            }
        }
    }
}
