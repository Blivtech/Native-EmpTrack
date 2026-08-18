package com.blivtech.emptrack.ui.advance.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.databinding.ItemShiftPickerBinding

class ShiftPickerAdapter(
    private val onSelect: (ShiftEntity) -> Unit
) : RecyclerView.Adapter<ShiftPickerAdapter.ViewHolder>() {

    private var shifts = listOf<ShiftEntity>()

    fun submitList(list: List<ShiftEntity>) {
        shifts = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        val binding: ItemShiftPickerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(shift: ShiftEntity) {
            binding.tvShiftName.text = shift.shiftName
            binding.tvShiftTime.text =
                "${shift.startTime.toString().take(5)} – ${shift.endTime.toString().take(5)}"
            binding.root.setOnClickListener { onSelect(shift) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemShiftPickerBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(shifts[position])

    override fun getItemCount() = shifts.size
}