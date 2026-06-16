package com.blivtech.emptrack.ui.shiftplan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.model.ShiftPlanCardItem
import com.blivtech.emptrack.databinding.ItemShiftPlanCardBinding

class ShiftPlanAdapter(
    private val onAssignClick: (ShiftPlanCardItem) -> Unit
) : ListAdapter<ShiftPlanCardItem, ShiftPlanAdapter.ViewHolder>(DiffCallback()) {

    // ─────────────────────────────────
    // ✅ ViewHolder
    // ─────────────────────────────────
    inner class ViewHolder(
        private val binding: ItemShiftPlanCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ShiftPlanCardItem) {
            val shift    = item.shift
            val empCount = item.empCount

            // ✅ Shift index label
            binding.tvShiftIndex.text = "Shift ${item.index + 1}"

            // ✅ Shift name + time
            binding.tvShiftName.text = shift.shiftName
            binding.tvShiftTime.text =
                "${shift.startTime.toString().take(5)} – " +
                "${shift.endTime.toString().take(5)}"

            // ✅ Emp count
            binding.tvShiftEmpCount.text = "$empCount emp"

            // ✅ Emp names
            binding.tvShiftEmployees.text = if (item.empNames.isNotEmpty()) {
                item.empNames.joinToString(", ") +
                if (empCount > 4) " +${empCount - 4} more" else ""
            } else {
                "No employees assigned"
            }

            // ✅ Button text
            binding.btnAssign.text = if (empCount > 0) "Edit plan" else "Assign now"
            binding.btnAssign.setOnClickListener {
                onAssignClick(item)
            }

            // ✅ Alert visibility
            binding.layoutAlert.visibility =
                if (empCount == 0) View.VISIBLE else View.GONE

            // ✅ Shift icon color by index
            when (item.index % 3) {
                0 -> binding.layoutShiftIcon
                    .setBackgroundResource(R.drawable.bg_shift_morning)
                1 -> binding.layoutShiftIcon
                    .setBackgroundResource(R.drawable.bg_shift_evening)
                2 -> binding.layoutShiftIcon
                    .setBackgroundResource(R.drawable.bg_shift_night)
            }
        }
    }

    // ─────────────────────────────────
    // ✅ DiffCallback
    // ─────────────────────────────────
    class DiffCallback : DiffUtil.ItemCallback<ShiftPlanCardItem>() {
        override fun areItemsTheSame(
            oldItem: ShiftPlanCardItem,
            newItem: ShiftPlanCardItem
        ) = oldItem.shift.shiftCode == newItem.shift.shiftCode

        override fun areContentsTheSame(
            oldItem: ShiftPlanCardItem,
            newItem: ShiftPlanCardItem
        ) = oldItem == newItem
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemShiftPlanCardBinding.inflate(
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