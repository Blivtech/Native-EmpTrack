package com.blivtech.emptrack.ui.company.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.data.model.CompanyRequest
import com.blivtech.emptrack.databinding.ItemShiftFormBinding

class ShiftAdapter(
    private val onDelete: (Int) -> Unit
) : ListAdapter<CompanyRequest.ShiftRequest, ShiftAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(val binding: ItemShiftFormBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CompanyRequest.ShiftRequest, position: Int) {
            binding.tvShiftNumber.text = (position + 1).toString()
            binding.tvShiftName.text = item.shiftName
            binding.tvShiftTime.text = "${item.startTime} → ${item.endTime}"
            binding.ivDeleteShift.setOnClickListener { onDelete(position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemShiftFormBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), position)

    class DiffCallback : DiffUtil.ItemCallback<CompanyRequest.ShiftRequest>() {
        override fun areItemsTheSame(a: CompanyRequest.ShiftRequest, b: CompanyRequest.ShiftRequest) =
            a.shiftName == b.shiftName
        override fun areContentsTheSame(a: CompanyRequest.ShiftRequest, b: CompanyRequest.ShiftRequest) =
            a == b
    }
}