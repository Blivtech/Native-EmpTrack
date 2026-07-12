package com.blivtech.emptrack.ui.employee.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.data.model.EmployeeWithDetails
import com.blivtech.emptrack.databinding.ItemEmployeeBinding

class EmployeeAdapter(
    private val onClick: (EmployeeWithDetails) -> Unit,
    private val onCall: (EmployeeWithDetails) -> Unit
) : ListAdapter<EmployeeWithDetails, EmployeeAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(val binding: ItemEmployeeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(employee: EmployeeWithDetails) {
            // ✅ Initials
            val initials = employee.name.split(" ")
                .take(2).joinToString("") { it.first().uppercase() }
            binding.tvInitials.text = initials

            // ✅ Info
            binding.tvEmpName.text = employee.name
            binding.tvEmpRole.text = "${employee.desgName}"
            binding.tvEmpDept.text = "${employee.phone}"

            // ✅ Status badge
            binding.tvStatus.text = if (employee.status == 1) "Active" else "Inactive"
            binding.tvStatus.setBackgroundResource(
                if (employee.status == 1)
                    com.blivtech.emptrack.R.drawable.bg_badge_green_light
                else
                    com.blivtech.emptrack.R.drawable.bg_badge_red
            )

            // ✅ Click
            binding.root.setOnClickListener { onClick(employee) }
            binding.ivCall.setOnClickListener { onCall(employee) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemEmployeeBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<EmployeeWithDetails>() {
        override fun areItemsTheSame(a: EmployeeWithDetails, b: EmployeeWithDetails) = a.id == b.id
        override fun areContentsTheSame(a: EmployeeWithDetails, b: EmployeeWithDetails) = a == b
    }
}