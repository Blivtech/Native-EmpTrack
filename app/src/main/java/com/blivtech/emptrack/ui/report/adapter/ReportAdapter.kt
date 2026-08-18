package com.blivtech.emptrack.ui.report.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.data.model.ReportItem
import com.blivtech.emptrack.databinding.ItemReportCardBinding

class ReportAdapter(
    private val onClick: (ReportItem) -> Unit
) : ListAdapter<ReportItem, ReportAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(
        private val binding: ItemReportCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReportItem) {
            // Icon glyph + colour
            binding.ivReportIcon.setImageResource(item.iconRes)
            binding.ivReportIcon.setColorFilter(Color.parseColor(item.iconTintColor))

            // Rounded chip background — tint keeps the corner radius
            binding.layoutReportIcon.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(item.iconBgColor))

            binding.tvReportName.text     = item.name
            binding.tvReportSubtitle.text = item.subtitle

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ReportItem>() {
        override fun areItemsTheSame(a: ReportItem, b: ReportItem) = a.id == b.id
        override fun areContentsTheSame(a: ReportItem, b: ReportItem) = a == b
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemReportCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))
}