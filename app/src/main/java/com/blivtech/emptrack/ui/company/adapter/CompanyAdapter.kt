package com.blivtech.emptrack.ui.company.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.R
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

    data class CompanyWithShifts(
        val company: CompanyEntity,
        val shifts: List<ShiftEntity>
    )

    inner class ViewHolder(val binding: ItemCompanyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private fun dp(v: Float) = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, v, itemView.resources.displayMetrics
        ).toInt()

        fun bind(item: CompanyWithShifts) {
            val company = item.company
            val ctx = binding.root.context

            // Avatar — initial + rotating colour
            binding.tvCompanyInitial.text = company.name.firstOrNull()?.uppercase() ?: "C"
            val (avaBg, avaFg) = AVATAR_PALETTE[position.coerceAtLeast(0) % AVATAR_PALETTE.size]
            binding.tvCompanyInitial.backgroundTintList = ColorStateList.valueOf(avaBg)
            binding.tvCompanyInitial.setTextColor(avaFg)

            // Info
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

            // Selected state — blue ring + "Active" pill
            val isSelected = company.id == selectedCompanyId
            binding.root.strokeColor = BRAND
            binding.root.strokeWidth = if (isSelected) dp(1.5f) else 0
            binding.layoutSelected.visibility = if (isSelected) View.VISIBLE else View.GONE

            // Shift chips — green clock pills
            binding.chipGroupShifts.removeAllViews()
            item.shifts.forEach { shift ->
                val chip = Chip(ctx).apply {
                    text = "${shift.shiftName} · ${shift.startTime.take(5)}–${shift.endTime.take(5)}"
                    textSize = 11f
                    setTextColor(CHIP_TEXT)
                    isClickable = false
                    isCheckable = false
                    chipBackgroundColor = ColorStateList.valueOf(CHIP_BG)
                    chipStrokeWidth = 0f
                    chipCornerRadius = dp(12f).toFloat()
                    chipMinHeight = dp(30f).toFloat()
                    chipIcon = ContextCompat.getDrawable(ctx, R.drawable.ic_clock)
                    chipIconTint = ColorStateList.valueOf(CHIP_ICON)
                    chipIconSize = dp(14f).toFloat()
                    iconStartPadding = dp(2f).toFloat()
                    textStartPadding = dp(4f).toFloat()
                    setEnsureMinTouchTargetSize(false)
                }
                binding.chipGroupShifts.addView(chip)
            }

            // Clicks
            binding.root.setOnClickListener { onSelect(company) }
            binding.ivEdit.setOnClickListener { onEdit(company) }
            binding.ivDelete.setOnClickListener { onDelete(company) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCompanyBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<CompanyWithShifts>() {
        override fun areItemsTheSame(a: CompanyWithShifts, b: CompanyWithShifts) =
            a.company.id == b.company.id
        override fun areContentsTheSame(a: CompanyWithShifts, b: CompanyWithShifts) = a == b
    }

    companion object {
        private val BRAND     = Color.parseColor("#2563EB")
        private val CHIP_BG   = Color.parseColor("#DCFCE7")
        private val CHIP_TEXT = Color.parseColor("#166534")
        private val CHIP_ICON = Color.parseColor("#16A34A")

        // (avatar background, initial colour) — cycles per row
        private val AVATAR_PALETTE = listOf(
            Color.parseColor("#DBEAFE") to Color.parseColor("#1D4ED8"), // blue
            Color.parseColor("#EEF0FF") to Color.parseColor("#4F46E5"), // violet
            Color.parseColor("#FEF3E2") to Color.parseColor("#D97706"), // amber
            Color.parseColor("#DCFCE7") to Color.parseColor("#16A34A"), // green
            Color.parseColor("#FFE4E6") to Color.parseColor("#E11D48"), // rose
            Color.parseColor("#CCFBF1") to Color.parseColor("#0D9488")  // teal
        )
    }
}