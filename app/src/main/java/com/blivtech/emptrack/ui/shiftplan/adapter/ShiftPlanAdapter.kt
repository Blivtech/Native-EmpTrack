package com.blivtech.emptrack.ui.shiftplan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.ShiftEntity

class ShiftPlanAdapter(
    private val onAssignClick: (ShiftEntity) -> Unit,
    private val getEmpCount: (String) -> Int,
    private val getEmpNames: (String) -> List<String>
) : ListAdapter<ShiftEntity, ShiftPlanAdapter.ShiftViewHolder>(ShiftDiffCallback()) {

    // ─────────────────────────────────────────────────────────────────────
    // FIX 1: expose a way to re-bind all visible cards when weekPlan
    //         changes without changing the shift list itself.
    // ─────────────────────────────────────────────────────────────────────
    fun refreshCounts() {
        // Re-bind every currently-submitted item so emp counts are fresh.
        notifyItemRangeChanged(0, itemCount, PAYLOAD_COUNTS)
    }

    // ─────────────────────────────────────────────────────────────────────
    // ViewHolder
    // ─────────────────────────────────────────────────────────────────────
    inner class ShiftViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvShiftName: TextView     = view.findViewById(R.id.tvShiftName)
        val tvShiftTime: TextView     = view.findViewById(R.id.tvShiftTime)
        val tvShiftEmpCount: TextView = view.findViewById(R.id.tvShiftEmpCount)
        val tvShiftEmployees: TextView = view.findViewById(R.id.tvShiftEmployees)
        val btnAssign: Button         = view.findViewById(R.id.btnAssign)
        val layoutAlert: LinearLayout = view.findViewById(R.id.layoutAlert)
        val layoutShiftIcon: LinearLayout = view.findViewById(R.id.layoutShiftIcon)
        val tvShiftIndex: TextView    = view.findViewById(R.id.tvShiftIndex)
    }

    // ─────────────────────────────────────────────────────────────────────
    // Inflate
    // ─────────────────────────────────────────────────────────────────────
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShiftViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shift_plan_card, parent, false)
        return ShiftViewHolder(view)
    }

    // ─────────────────────────────────────────────────────────────────────
    // Full bind
    // ─────────────────────────────────────────────────────────────────────
    override fun onBindViewHolder(holder: ShiftViewHolder, position: Int) {
        val shift = getItem(position)
        bindShiftMeta(holder, shift, position)
        bindCounts(holder, shift)
    }

    // ─────────────────────────────────────────────────────────────────────
    // FIX 2: partial bind — only refresh count-related views when the
    //         PAYLOAD_COUNTS payload is delivered, avoiding a full rebind.
    // ─────────────────────────────────────────────────────────────────────
    override fun onBindViewHolder(
        holder: ShiftViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty() && payloads.all { it == PAYLOAD_COUNTS }) {
            bindCounts(holder, getItem(position))
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────
    private fun bindShiftMeta(
        holder: ShiftViewHolder,
        shift: ShiftEntity,
        position: Int
    ) {
        holder.tvShiftName.text  = shift.shiftName
        holder.tvShiftIndex.text = "Shift ${position + 1}"

        // Format "HH:mm" from LocalTime (or similar) safely
        val start = shift.startTime.toString().take(5)
        val end   = shift.endTime.toString().take(5)
        holder.tvShiftTime.text = "$start - $end"

        // Icon background cycles through morning / evening / night
        val bgRes = when (position % 3) {
            0    -> R.drawable.bg_shift_morning
            1    -> R.drawable.bg_shift_evening
            else -> R.drawable.bg_shift_night
        }
        holder.layoutShiftIcon.setBackgroundResource(bgRes)
    }

    private fun bindCounts(holder: ShiftViewHolder, shift: ShiftEntity) {
        val empCount = getEmpCount(shift.shiftCode)
        val empNames = getEmpNames(shift.shiftCode)

        holder.tvShiftEmpCount.text = "$empCount emp"

        holder.tvShiftEmployees.text = if (empNames.isNotEmpty()) {
            val display = empNames.take(4).joinToString(", ")
            if (empCount > 4) "$display +${empCount - 4} more" else display
        } else {
            "No employees assigned"
        }

        holder.btnAssign.text = if (empCount > 0) "Edit Plan" else "Assign Now"

        holder.layoutAlert.visibility =
            if (empCount == 0) View.VISIBLE else View.GONE

        holder.btnAssign.setOnClickListener { onAssignClick(shift) }
    }

    // ─────────────────────────────────────────────────────────────────────
    // DiffCallback — uses shiftCode as the stable key
    // ─────────────────────────────────────────────────────────────────────
    private class ShiftDiffCallback : DiffUtil.ItemCallback<ShiftEntity>() {
        override fun areItemsTheSame(old: ShiftEntity, new: ShiftEntity) =
            old.shiftCode == new.shiftCode

        override fun areContentsTheSame(old: ShiftEntity, new: ShiftEntity) =
            old == new
    }

    companion object {
        private const val PAYLOAD_COUNTS = "payload_counts"
    }
}