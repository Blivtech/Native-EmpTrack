package com.blivtech.emptrack.ui.company.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.data.local.entity.CompanyEntity
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.databinding.ItemCompanyBinding
import com.google.android.material.chip.Chip

class CompanyAdapter(
    private val selectedCompanyId: Long,
    private val onSelect: (CompanyEntity) -> Unit,
    private val onEdit: (CompanyEntity) -> Unit,
    private val onDelete: (CompanyEntity) -> Unit
) : ListAdapter<CompanyAdapter.CompanyWithShifts, CompanyAdapter.ViewHolder>(DiffCallback()) {

    // ✅ Wrapper to hold company + its shifts
    data class CompanyWithShifts(
        val company: CompanyEntity,
        val shifts: List<ShiftEntity>
    )

    inner class ViewHolder(val binding: ItemCompanyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CompanyWithShifts) {
            val company = item.company
            val shifts = item.shifts

            // ✅ Company initial letter
            binding.tvCompanyInitial.text = company.name
                .firstOrNull()?.uppercase() ?: "C"

            // ✅ Company info
            binding.tvCompanyName.text = company.name
            binding.tvCompanyCity.text = buildString {
                if (!company.city.isNullOrEmpty()) append(company.city)
                if (!company.state.isNullOrEmpty()) {
                    if (isNotEmpty()) append(", ")
                    append(company.state)
                }
                if (isEmpty()) append("No location")
            }
            binding.tvCompanyPhone.text = company.phone ?: "No phone"

            // ✅ Highlight selected
            val isSelected = company.id == selectedCompanyId
            binding.root.strokeWidth = if (isSelected) 4 else 0
            binding.layoutSelected.visibility =
                if (isSelected) android.view.View.VISIBLE else android.view.View.GONE

            // ✅ Shift chips
            binding.chipGroupShifts.removeAllViews()
            shifts.forEach { shift ->
                val chip = Chip(binding.root.context).apply {
                    text = "${shift.shiftName} ${shift.startTime.take(5)}–${shift.endTime.take(5)}"
                    textSize = 10f
                    isClickable = false
                    setChipBackgroundColorResource(com.blivtech.emptrack.R.color.chip_bg)
                    setTextColor(
                        binding.root.context.getColor(
                            com.blivtech.emptrack.R.color.chip_text
                        )
                    )
                    chipCornerRadius=4f
                }
                binding.chipGroupShifts.addView(chip)
            }

            // ✅ Click listeners
            binding.root.setOnClickListener { onSelect(company) }
            binding.ivEdit.setOnClickListener { onEdit(company) }
            binding.ivDelete.setOnClickListener { onDelete(company) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemCompanyBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<CompanyWithShifts>() {
        override fun areItemsTheSame(a: CompanyWithShifts, b: CompanyWithShifts) =
            a.company.id == b.company.id
        override fun areContentsTheSame(a: CompanyWithShifts, b: CompanyWithShifts) =
            a == b
    }
}