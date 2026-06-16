package com.blivtech.emptrack.ui.attendance

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
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

    // ─────────────────────────────────
    // ✅ ViewHolder
    // ─────────────────────────────────
    inner class ViewHolder(
        private val binding: ItemAttendanceShiftCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ShiftCardItem) {
            val shift  = item.shift
            val status = item.status

            // ✅ Shift icon color — by index
            when (item.index % 3) {
                0 -> {
                    binding.cardShift.setBackgroundResource(
                        R.drawable.bg_shift_morning
                    )
                    binding.ivShiftIcon.setColorFilter(
                        Color.parseColor("#27500A")
                    )
                }
                1 -> {
                    binding.layoutShiftIcon.setBackgroundResource(
                        R.drawable.bg_shift_evening
                    )
                    binding.ivShiftIcon.setColorFilter(
                        Color.parseColor("#0C447C")
                    )
                }
                2 -> {
                    binding.layoutShiftIcon.setBackgroundResource(
                        R.drawable.bg_shift_night
                    )
                    binding.ivShiftIcon.setColorFilter(
                        Color.parseColor("#534AB7")
                    )
                }
            }

            // ✅ Shift name + time
            binding.tvShiftName.text =
                "Shift ${item.index + 1} · ${shift.shiftName}"
            binding.tvShiftTime.text =
                "${shift.startTime.take(5)} – ${shift.endTime.take(5)}"

            // ✅ Bind by state
            if (status != null && status.isMarked) {
                bindMarked(item, status)
            } else {
                bindNotMarked(item)
            }
        }

        // ✅ Marked state
        private fun bindMarked(
            item: ShiftCardItem,
            status: ShiftStatusResponse
        ) {
            // ── Counts ──
            val total   = status.presentCount.toInt() +
                    status.absentCount           +
                    status.weekoffCount          +
                    status.leaveCount            +
                    status.holidayCount
            val present = status.presentCount.toInt()
            val pct     = if (total > 0) (present * 100 / total) else 0

            // ── Badge ──
            binding.tvShiftBadge.text = "Marked ✓"
            binding.tvShiftBadge.setBackgroundResource(R.drawable.bg_badge_green)
            binding.tvShiftBadge.setTextColor(Color.parseColor("#27500A"))

            // ── Count text ──
            binding.tvShiftCount.text =
                "$present present · ${status.absentCount} absent · ${status.weekoffCount} off"
            binding.tvShiftCount.setTextColor(Color.parseColor("#27500A"))

            // ── Progress ──
            binding.progShift.progress = pct
            binding.progShift.progressDrawable =
                binding.root.context.getDrawable(R.drawable.progress_drawable)

            // ── Button ──
            binding.btnMarkAttendance.text = "Edit"
            binding.btnMarkAttendance.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#27500A"))
            binding.btnMarkAttendance.setOnClickListener {
                onEditClick(item)
            }

            // ── Card stroke green ──
            binding.root.strokeColor = Color.parseColor("#639922")
            binding.root.strokeWidth = 2
        }

        // ✅ Not marked state
        private fun bindNotMarked(item: ShiftCardItem) {

            // ── Badge ──
            binding.tvShiftBadge.text = "Not marked"
            binding.tvShiftBadge.setBackgroundResource(R.drawable.bg_badge_blue)
            binding.tvShiftBadge.setTextColor(Color.parseColor("#0C447C"))

            // ── Count text ──
            binding.tvShiftCount.text = "Tap to mark attendance"
            binding.tvShiftCount.setTextColor(Color.parseColor("#757575"))

            // ── Progress ──
            binding.progShift.progress = 0
            binding.progShift.progressTintList =
                ColorStateList.valueOf(Color.parseColor("#1565C0"))

            // ── Button ──
            binding.btnMarkAttendance.text = "Mark now"
            binding.btnMarkAttendance.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#1565C0"))
            binding.btnMarkAttendance.setOnClickListener {
                onMarkClick(item)
            }

            // ── Card stroke grey ──
            binding.root.strokeColor = Color.parseColor("#E0E0E0")
            binding.root.strokeWidth = 0
        }
    }

    // ─────────────────────────────────
    // ✅ DiffCallback
    // ─────────────────────────────────
    class DiffCallback : DiffUtil.ItemCallback<ShiftCardItem>() {
        override fun areItemsTheSame(
            oldItem: ShiftCardItem,
            newItem: ShiftCardItem
        ) = oldItem.shift.shiftCode == newItem.shift.shiftCode

        override fun areContentsTheSame(
            oldItem: ShiftCardItem,
            newItem: ShiftCardItem
        ) = oldItem == newItem
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemAttendanceShiftCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) = holder.bind(getItem(position))
}