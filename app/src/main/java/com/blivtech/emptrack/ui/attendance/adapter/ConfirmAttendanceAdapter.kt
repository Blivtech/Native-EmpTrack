package com.blivtech.emptrack.ui.attendance.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.data.model.AttendanceDetailRequest
import com.blivtech.emptrack.databinding.ItemConfirmEmployeeBinding

class ConfirmAttendanceAdapter :
    RecyclerView.Adapter<ConfirmAttendanceAdapter.ViewHolder>() {

    private var employees = listOf<EmployeeEntity>()
    private var statusMap = mapOf<String, AttendanceDetailRequest>()

    fun submitList(
        list: List<EmployeeEntity>,
        map: Map<String, AttendanceDetailRequest>
    ) {
        employees = list
        statusMap = map
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        val binding: ItemConfirmEmployeeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(emp: EmployeeEntity) {
            val initials = emp.name.split(" ")
                .take(2).joinToString("") { it.first().uppercase() }
            binding.tvInitials.text = initials
            binding.tvEmpName.text  = emp.name
            binding.tvEmpCode.text  = emp.empCode

            val detail = statusMap[emp.empCode]
            when (detail?.dayPlanStatus) {
                1 -> {
                    binding.tvBadge.text = if (detail.workType == 1)
                        "Full day" else "Half day"
                    binding.tvBadge.setBackgroundResource(
                        if (detail.workType == 1) R.drawable.bg_badge_green
                        else R.drawable.bg_badge_amber
                    )
                }
                2 -> {
                    binding.tvBadge.text = "Week off"
                    binding.tvBadge.setBackgroundResource(R.drawable.bg_badge_amber)
                }
                3 -> {
                    binding.tvBadge.text = "Leave"
                    binding.tvBadge.setBackgroundResource(R.drawable.bg_badge_blue)
                }
                4 -> {
                    binding.tvBadge.text = "Holiday"
                    binding.tvBadge.setBackgroundResource(R.drawable.bg_badge_purple)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemConfirmEmployeeBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(employees[position])

    override fun getItemCount() = employees.size
}