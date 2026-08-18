package com.blivtech.emptrack.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.data.model.Banner
import com.blivtech.emptrack.databinding.ItemBannerBinding

class BannerAdapter(
    private val items: List<Banner>,
    private val onCtaClick: (Banner) -> Unit
) : RecyclerView.Adapter<BannerAdapter.VH>() {

    inner class VH(val b: ItemBannerBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.bannerRoot.setBackgroundResource(item.bgRes)
        holder.b.tvBadge.text       = item.badge
        holder.b.tvBannerTitle.text = item.title
        holder.b.tvBannerSub.text   = item.subtitle
        holder.b.tvBannerCta.text   = item.cta
        holder.b.tvBannerCta.setOnClickListener { onCtaClick(item) }
    }
}