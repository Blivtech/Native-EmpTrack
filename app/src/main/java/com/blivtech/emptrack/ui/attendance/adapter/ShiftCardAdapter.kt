package com.blivtech.emptrack.ui.attendance

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.model.ShiftStatusResponse
import com.blivtech.emptrack.databinding.ItemAttendanceShiftCardBinding

class ShiftCardAdapter(
    private val onMarkClick: (ShiftCardItem) -> Unit,
    private val onEditClick: (ShiftCardItem) -> Unit
) : ListAdapter<ShiftCardItem, ShiftCardAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(
        private val binding: ItemAttendanceShiftCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private fun dp(value: Float) = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value, itemView.resources.displayMetrics
        ).toInt()

        fun bind(item: ShiftCardItem) {
            val shift = item.shift

            // ── Shift icon chip — rotating palette (chip bg + glyph) ──
            val (chipBg, glyph) = ICON_PALETTE[item.index % ICON_PALETTE.size]
            binding.layoutShiftIcon.backgroundTintList = ColorStateList.valueOf(chipBg)
            binding.ivShiftIcon.setColorFilter(glyph)

            // ── Name + time ──
            binding.tvShiftName.text = "Shift ${item.index + 1} · ${shift.shiftName}"
            binding.tvShiftTime.text = "${shift.startTime.take(5)} – ${shift.endTime.take(5)}"

            // ── State ──
            val status = item.status
            if (status != null && status.isMarked) bindMarked(item, status)
            else                                   bindNotMarked(item)
        }

        // ── Marked ──
        private fun bindMarked(item: ShiftCardItem, status: ShiftStatusResponse) {
            val total = status.presentCount.toInt() + status.absentCount +
                    status.weekoffCount + status.leaveCount + status.holidayCount
            val present = status.presentCount.toInt()
            val pct = if (total > 0) present * 100 / total else 0

            binding.tvShiftBadge.text = "Marked ✓"
            binding.tvShiftBadge.setBackgroundResource(R.drawable.bg_badge_green_light)
            binding.tvShiftBadge.setTextColor(GREEN)

            binding.tvShiftCount.text =
                "$present present · ${status.absentCount} absent · ${status.weekoffCount} off"
            binding.tvShiftCount.setTextColor(GREEN)

            binding.progShift.progressDrawable =
                ContextCompat.getDrawable(itemView.context, R.drawable.progress_shift)
            binding.progShift.progressTintList = ColorStateList.valueOf(GREEN)
            binding.progShift.progress = pct

            binding.btnMarkAttendance.text = "Edit attendance"
            binding.btnMarkAttendance.backgroundTintList = ColorStateList.valueOf(GHOST_BG)
            binding.btnMarkAttendance.setTextColor(BRAND)
            binding.btnMarkAttendance.setOnClickListener { onEditClick(item) }

            binding.root.strokeColor = GREEN_STROKE
            binding.root.strokeWidth = dp(1.5f)
        }

        // ── Not marked ──
        private fun bindNotMarked(item: ShiftCardItem) {
            binding.tvShiftBadge.text = "Not marked"
            binding.tvShiftBadge.setBackgroundResource(R.drawable.bg_badge_blue_light)
            binding.tvShiftBadge.setTextColor(BRAND)

            binding.tvShiftCount.text = "Tap to mark attendance"
            binding.tvShiftCount.setTextColor(MUTED)

            binding.progShift.progressDrawable =
                ContextCompat.getDrawable(itemView.context, R.drawable.progress_shift)
            binding.progShift.progress = 0

            binding.btnMarkAttendance.text = "Mark now"
            binding.btnMarkAttendance.backgroundTintList = ColorStateList.valueOf(BRAND)
            binding.btnMarkAttendance.setTextColor(Color.WHITE)
            binding.btnMarkAttendance.setOnClickListener { onMarkClick(item) }

            binding.root.strokeWidth = 0
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ShiftCardItem>() {
        override fun areItemsTheSame(a: ShiftCardItem, b: ShiftCardItem) =
            a.shift.shiftCode == b.shift.shiftCode

        override fun areContentsTheSame(a: ShiftCardItem, b: ShiftCardItem) = a == b
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemAttendanceShiftCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        private val GREEN        = Color.parseColor("#16A34A")
        private val GREEN_STROKE = Color.parseColor("#86EFAC")
        private val BRAND        = Color.parseColor("#2563EB")
        private val GHOST_BG     = Color.parseColor("#EEF4FF")
        private val MUTED        = Color.parseColor("#94A3B8")

        // (chip background, glyph colour) — cycles per shift index
        private val ICON_PALETTE = listOf(
            Color.parseColor("#EEF4FF") to Color.parseColor("#2563EB"), // blue
            Color.parseColor("#F3E8FF") to Color.parseColor("#9333EA"), // violet
            Color.parseColor("#DCFCE7") to Color.parseColor("#16A34A"), // green
            Color.parseColor("#FEF3E2") to Color.parseColor("#D97706")  // amber
        )
    }
}