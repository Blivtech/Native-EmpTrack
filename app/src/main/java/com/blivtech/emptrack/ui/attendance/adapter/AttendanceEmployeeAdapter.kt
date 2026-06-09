package com.blivtech.emptrack.ui.attendance.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.data.model.AttendanceDetailRequest
import com.blivtech.emptrack.databinding.ItemAttendanceEmployeeBinding

class AttendanceEmployeeAdapter(
    private val onStatusChanged: (
        empCode: String,
        status: Int,
        workType: Int
    ) -> Unit
) : RecyclerView.Adapter<AttendanceEmployeeAdapter.ViewHolder>() {

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
        val binding: ItemAttendanceEmployeeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(emp: EmployeeEntity) {
            val initials = emp.name.split(" ")
                .take(2).joinToString("") { it.first().uppercase() }
            binding.tvInitials.text = initials
            binding.tvEmpName.text  = emp.name
            binding.tvEmpCode.text  = emp.empCode

            val current = statusMap[emp.empCode]
            resetAll()

            current?.let { detail ->
                when (detail.dayPlanStatus) {
                    1 -> {
                        applyWorking()
                        binding.layoutSubOptions.visibility = View.VISIBLE
                        if (detail.workType == 1) applyFull() else applyHalf()
                        binding.tvStatusBadge.visibility = View.VISIBLE
                        binding.tvStatusBadge.text = if (detail.workType == 1)
                            "Full day ✓" else "Half day ✓"
                        binding.tvStatusBadge.setBackgroundResource(
                            if (detail.workType == 1) R.drawable.bg_badge_green
                            else R.drawable.bg_badge_amber
                        )
                        binding.root.setBackgroundResource(R.drawable.bg_card_working)
                    }
                    2 -> {
                        applyWeekoff()
                        binding.tvStatusBadge.visibility = View.VISIBLE
                        binding.tvStatusBadge.text = "Week off ✓"
                        binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_amber)
                        binding.root.setBackgroundResource(R.drawable.bg_card_weekoff)
                    }
                    3 -> {
                        applyLeave()
                        binding.tvStatusBadge.visibility = View.VISIBLE
                        binding.tvStatusBadge.text = "Leave ✓"
                        binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_blue)
                        binding.root.setBackgroundResource(R.drawable.bg_card_leave)
                    }
                    4 -> {
                        applyHoliday()
                        binding.tvStatusBadge.visibility = View.VISIBLE
                        binding.tvStatusBadge.text = "Holiday ✓"
                        binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_purple)
                        binding.root.setBackgroundResource(R.drawable.bg_card_holiday)
                    }
                }
            } ?: run {
                binding.layoutSubOptions.visibility = View.GONE
                binding.tvStatusBadge.visibility    = View.GONE
                binding.root.setBackgroundResource(R.drawable.bg_card_unselected)
            }

            // ✅ Status clicks
            binding.btnWorking.setOnClickListener {
                resetAll()
                applyWorking()
                binding.layoutSubOptions.visibility = View.VISIBLE
                applyFull() // ✅ Auto Full day
                onStatusChanged(emp.empCode, 1, 1)
            }
            binding.btnWeekoff.setOnClickListener {
                resetAll()
                applyWeekoff()
                onStatusChanged(emp.empCode, 2, 1)
            }
            binding.btnLeave.setOnClickListener {
                resetAll()
                applyLeave()
                onStatusChanged(emp.empCode, 3, 1)
            }
            binding.btnHoliday.setOnClickListener {
                resetAll()
                applyHoliday()
                onStatusChanged(emp.empCode, 4, 1)
            }
            binding.btnFullDay.setOnClickListener {
                applyFull()
                onStatusChanged(emp.empCode, 1, 1)
            }
            binding.btnHalfDay.setOnClickListener {
                applyHalf()
                onStatusChanged(emp.empCode, 1, 2)
            }
        }

        private fun resetAll() {
            listOf(
                binding.btnWorking,
                binding.btnWeekoff,
                binding.btnLeave,
                binding.btnHoliday,
                binding.btnFullDay,
                binding.btnHalfDay
            ).forEach { it.setBackgroundResource(R.drawable.bg_field_trigger) }
            binding.layoutSubOptions.visibility = View.GONE
            binding.tvStatusBadge.visibility    = View.GONE
            binding.root.setBackgroundResource(R.drawable.bg_card_unselected)
        }

        private fun applyWorking() = binding.btnWorking.setBackgroundResource(R.drawable.bg_badge_green)
        private fun applyWeekoff() = binding.btnWeekoff.setBackgroundResource(R.drawable.bg_badge_amber)
        private fun applyLeave()   = binding.btnLeave.setBackgroundResource(R.drawable.bg_badge_blue)
        private fun applyHoliday() = binding.btnHoliday.setBackgroundResource(R.drawable.bg_badge_purple)
        private fun applyFull() {
            binding.btnFullDay.setBackgroundResource(R.drawable.bg_circle_blue)
            binding.btnHalfDay.setBackgroundResource(R.drawable.bg_field_trigger)
        }
        private fun applyHalf() {
            binding.btnHalfDay.setBackgroundResource(R.drawable.bg_circle_amber)
            binding.btnFullDay.setBackgroundResource(R.drawable.bg_field_trigger)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemAttendanceEmployeeBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(employees[position])

    override fun getItemCount() = employees.size
}