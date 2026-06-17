package com.blivtech.emptrack.ui.report.adapter

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
            // ✅ Icon
            binding.ivReportIcon.setImageResource(item.iconRes)
            binding.ivReportIcon.setColorFilter(
                Color.parseColor(item.iconTintColor)
            )
            binding.layoutReportIcon.setBackgroundColor(
                Color.parseColor(item.iconBgColor)
            )

            // ✅ Name + subtitle
            binding.tvReportName.text     = item.name
            binding.tvReportSubtitle.text = item.subtitle

            // ✅ Tag
            binding.tvReportTag.text = item.tag

            // ✅ Tag color by category
            when (item.category) {
                "ATTENDANCE" -> {
                    binding.tvReportTag.setTextColor(
                        Color.parseColor("#0C447C")
                    )
                    binding.tvReportTag.setBackgroundResource(
                        com.blivtech.emptrack.R.drawable.bg_badge_blue
                    )
                }
                "WAGES" -> {
                    binding.tvReportTag.setTextColor(
                        Color.parseColor("#633806")
                    )
                    binding.tvReportTag.setBackgroundResource(
                        com.blivtech.emptrack.R.drawable.bg_badge_amber
                    )
                }
                "WORK" -> {
                    binding.tvReportTag.setTextColor(
                        Color.parseColor("#E65100")
                    )
                    binding.tvReportTag.setBackgroundResource(
                        com.blivtech.emptrack.R.drawable.bg_badge_red_light
                    )
                }
            }

            // ✅ Click
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ReportItem>() {
        override fun areItemsTheSame(
            oldItem: ReportItem, newItem: ReportItem
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ReportItem, newItem: ReportItem
        ) = oldItem == newItem
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        val binding = ItemReportCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder, position: Int
    ) = holder.bind(getItem(position))
}