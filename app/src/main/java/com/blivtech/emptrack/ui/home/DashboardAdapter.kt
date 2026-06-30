package com.blivtech.emptrack.ui.home

import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.model.DashboardItem

class DashboardAdapter(
    private val items: List<DashboardItem>,
    private val onClick: (DashboardItem) -> Unit
) : RecyclerView.Adapter<DashboardAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val iconHolder: View = view.findViewById(R.id.iconHolder)
        val icon: ImageView = view.findViewById(R.id.icon)
        val title: TextView = view.findViewById(R.id.title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard, parent, false)
        return VH(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.title.text = item.title
        holder.icon.setImageResource(item.icon)

        // Tint the glyph
        holder.icon.setColorFilter(
            ContextCompat.getColor(context, item.iconColor),
            PorterDuff.Mode.SRC_IN
        )

        // Tint the rounded-square background (keeps the 18dp corner radius)
        (holder.iconHolder.background.mutate() as GradientDrawable)
            .setColor(ContextCompat.getColor(context, item.iconBgColor))

        holder.itemView.setOnClickListener { onClick(item) }
    }
}