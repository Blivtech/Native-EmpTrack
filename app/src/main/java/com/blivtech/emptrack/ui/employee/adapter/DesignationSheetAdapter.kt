package com.blivtech.emptrack.ui.employee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.DesignationEntity

class DesignationSheetAdapter(
    private val selectedId: Long?,
    private val onClick: (DesignationEntity) -> Unit
) : ListAdapter<DesignationEntity, DesignationSheetAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvItemName)
        val tvSub: TextView = view.findViewById(R.id.tvItemSub)
        val ivCheck: ImageView = view.findViewById(R.id.ivItemCheck)

        fun bind(item: DesignationEntity) {
            tvName.text = item.name
            tvSub.text = item.description ?: ""

            // ✅ Show check if selected
            ivCheck.visibility = if (item.id == selectedId) View.VISIBLE else View.GONE

            // ✅ Highlight selected item
            itemView.setBackgroundResource(
                if (item.id == selectedId) R.drawable.bg_item_selected
                else android.R.color.transparent
            )

            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sheet_option, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<DesignationEntity>() {
        override fun areItemsTheSame(
            a: DesignationEntity,
            b: DesignationEntity
        ) = a.id == b.id

        override fun areContentsTheSame(
            a: DesignationEntity,
            b: DesignationEntity
        ) = a == b
    }
}