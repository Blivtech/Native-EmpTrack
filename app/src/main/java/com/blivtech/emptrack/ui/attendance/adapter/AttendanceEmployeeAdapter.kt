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

    // ─────────────────────────────────────
    inner class ViewHolder(
        val binding: ItemAttendanceEmployeeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(emp: EmployeeEntity) {

            // ✅ Initials
            val initials = emp.name
                .split(" ")
                .take(2)
                .joinToString("") { it.first().uppercase() }
            binding.tvInitials.text = initials
            binding.tvEmpName.text  = emp.name
            binding.tvEmpCode.text  = emp.empCode

            // ✅ Get current status
            val current = statusMap[emp.empCode]

            // ✅ Reset everything first
            resetAll()

            if (current != null) {
                // ✅ Apply status
                when (current.dayPlanStatus) {

                    // ─── Working ───────────────────────
                    1 -> {
                        // Status button
                        binding.btnWorking.setBackgroundResource(
                            R.drawable.bg_badge_green
                        )
                        binding.btnWorking.setTextColor(
                            binding.root.context.getColor(
                                android.R.color.white
                            )
                        )

                        // ✅ Show Full/Half sub-options
                        binding.layoutSubOptions.visibility = View.VISIBLE

                        // ✅ Apply full or half day
                        if (current.workType == 2) {
                            applyHalf()
                            binding.tvStatusBadge.text = "Half day ✓"
                            binding.tvStatusBadge.setBackgroundResource(
                                R.drawable.bg_badge_amber
                            )
                        } else {
                            applyFull()
                            binding.tvStatusBadge.text = "Full day ✓"
                            binding.tvStatusBadge.setBackgroundResource(
                                R.drawable.bg_badge_green
                            )
                        }

                        binding.tvStatusBadge.visibility = View.VISIBLE
                        binding.root.setBackgroundResource(
                            R.drawable.bg_card_working
                        )
                    }

                    // ─── Week off ──────────────────────
                    2 -> {
                        binding.btnWeekoff.setBackgroundResource(
                            R.drawable.bg_badge_amber
                        )
                        binding.btnWeekoff.setTextColor(
                            binding.root.context.getColor(
                                android.R.color.white
                            )
                        )
                        binding.layoutSubOptions.visibility = View.GONE
                        binding.tvStatusBadge.text = "Week off ✓"
                        binding.tvStatusBadge.setBackgroundResource(
                            R.drawable.bg_badge_amber
                        )
                        binding.tvStatusBadge.visibility = View.VISIBLE
                        binding.root.setBackgroundResource(
                            R.drawable.bg_card_weekoff
                        )
                    }

                    // ─── Leave ─────────────────────────
                    3 -> {
                        binding.btnLeave.setBackgroundResource(
                            R.drawable.bg_badge_blue
                        )
                        binding.btnLeave.setTextColor(
                            binding.root.context.getColor(
                                android.R.color.white
                            )
                        )
                        binding.layoutSubOptions.visibility = View.GONE
                        binding.tvStatusBadge.text = "Leave ✓"
                        binding.tvStatusBadge.setBackgroundResource(
                            R.drawable.bg_badge_blue
                        )
                        binding.tvStatusBadge.visibility = View.VISIBLE
                        binding.root.setBackgroundResource(
                            R.drawable.bg_card_leave
                        )
                    }

                    // ─── Holiday ───────────────────────
                    4 -> {
                        binding.btnHoliday.setBackgroundResource(
                            R.drawable.bg_badge_purple
                        )
                        binding.btnHoliday.setTextColor(
                            binding.root.context.getColor(
                                android.R.color.white
                            )
                        )
                        binding.layoutSubOptions.visibility = View.GONE
                        binding.tvStatusBadge.text = "Holiday ✓"
                        binding.tvStatusBadge.setBackgroundResource(
                            R.drawable.bg_badge_purple
                        )
                        binding.tvStatusBadge.visibility = View.VISIBLE
                        binding.root.setBackgroundResource(
                            R.drawable.bg_card_holiday
                        )
                    }
                }
            }

            // ─────────────────────────────────────
            // ✅ Status button clicks
            // ─────────────────────────────────────

            binding.btnWorking.setOnClickListener {
                resetAll()

                // ✅ Highlight Working button
                binding.btnWorking.setBackgroundResource(R.drawable.bg_badge_green)
                binding.btnWorking.setTextColor(
                    binding.root.context.getColor(android.R.color.white)
                )

                // ✅ Show Full/Half sub-options
                binding.layoutSubOptions.visibility = View.VISIBLE

                // ✅ Auto select Full day by default
                applyFull()

                // ✅ Show badge
                binding.tvStatusBadge.text = "Full day ✓"
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_green)
                binding.tvStatusBadge.visibility = View.VISIBLE

                // ✅ Card background
                binding.root.setBackgroundResource(R.drawable.bg_card_working)

                onStatusChanged(emp.empCode, 1, 1)
            }

            binding.btnWeekoff.setOnClickListener {
                resetAll()

                binding.btnWeekoff.setBackgroundResource(R.drawable.bg_badge_amber)
                binding.btnWeekoff.setTextColor(
                    binding.root.context.getColor(android.R.color.white)
                )
                binding.layoutSubOptions.visibility = View.GONE
                binding.tvStatusBadge.text = "Week off ✓"
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_amber)
                binding.tvStatusBadge.visibility = View.VISIBLE
                binding.root.setBackgroundResource(R.drawable.bg_card_weekoff)

                onStatusChanged(emp.empCode, 2, 1)
            }

            binding.btnLeave.setOnClickListener {
                resetAll()

                binding.btnLeave.setBackgroundResource(R.drawable.bg_badge_blue)
                binding.btnLeave.setTextColor(
                    binding.root.context.getColor(android.R.color.white)
                )
                binding.layoutSubOptions.visibility = View.GONE
                binding.tvStatusBadge.text = "Leave ✓"
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_blue)
                binding.tvStatusBadge.visibility = View.VISIBLE
                binding.root.setBackgroundResource(R.drawable.bg_card_leave)

                onStatusChanged(emp.empCode, 3, 1)
            }

            binding.btnHoliday.setOnClickListener {
                resetAll()

                binding.btnHoliday.setBackgroundResource(R.drawable.bg_badge_purple)
                binding.btnHoliday.setTextColor(
                    binding.root.context.getColor(android.R.color.white)
                )
                binding.layoutSubOptions.visibility = View.GONE
                binding.tvStatusBadge.text = "Holiday ✓"
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_purple)
                binding.tvStatusBadge.visibility = View.VISIBLE
                binding.root.setBackgroundResource(R.drawable.bg_card_holiday)

                onStatusChanged(emp.empCode, 4, 1)
            }

            // ─────────────────────────────────────
            // ✅ Full / Half day clicks
            // ─────────────────────────────────────

            binding.btnFullDay.setOnClickListener {
                applyFull()
                binding.tvStatusBadge.text = "Full day ✓"
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_green)
                binding.tvStatusBadge.visibility = View.VISIBLE
                onStatusChanged(emp.empCode, 1, 1)
            }

            binding.btnHalfDay.setOnClickListener {
                applyHalf()
                binding.tvStatusBadge.text = "Half day ✓"
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_amber)
                binding.tvStatusBadge.visibility = View.VISIBLE
                onStatusChanged(emp.empCode, 1, 2)
            }
        }

        // ─────────────────────────────────────
        // ✅ Helper functions
        // ─────────────────────────────────────

        private fun resetAll() {
            // ✅ Reset all status buttons
            listOf(
                binding.btnWorking,
                binding.btnWeekoff,
                binding.btnLeave,
                binding.btnHoliday
            ).forEach { btn ->
                btn.setBackgroundResource(R.drawable.bg_field_trigger)
                btn.setTextColor(
                    binding.root.context.getColor(android.R.color.darker_gray)
                )
            }

            // ✅ Reset sub-option buttons
            binding.btnFullDay.setBackgroundResource(R.drawable.bg_field_trigger)
            binding.btnFullDay.setTextColor(
                binding.root.context.getColor(android.R.color.darker_gray)
            )
            binding.btnHalfDay.setBackgroundResource(R.drawable.bg_field_trigger)
            binding.btnHalfDay.setTextColor(
                binding.root.context.getColor(android.R.color.darker_gray)
            )

            // ✅ Hide sub-options and badge
            binding.layoutSubOptions.visibility = View.GONE
            binding.tvStatusBadge.visibility    = View.GONE

            // ✅ Reset card background
            binding.root.setBackgroundResource(R.drawable.bg_card_unselected)
        }

        private fun applyFull() {
            // ✅ Full day selected — blue
            binding.btnFullDay.setBackgroundResource(R.drawable.bg_circle_blue)
            binding.btnFullDay.setTextColor(
                binding.root.context.getColor(android.R.color.white)
            )
            // ✅ Half day reset
            binding.btnHalfDay.setBackgroundResource(R.drawable.bg_field_trigger)
            binding.btnHalfDay.setTextColor(
                binding.root.context.getColor(android.R.color.darker_gray)
            )
        }

        private fun applyHalf() {
            // ✅ Half day selected — amber
            binding.btnHalfDay.setBackgroundResource(R.drawable.bg_circle_amber)
            binding.btnHalfDay.setTextColor(
                binding.root.context.getColor(android.R.color.white)
            )
            // ✅ Full day reset
            binding.btnFullDay.setBackgroundResource(R.drawable.bg_field_trigger)
            binding.btnFullDay.setTextColor(
                binding.root.context.getColor(android.R.color.darker_gray)
            )
        }
    }

    // ─────────────────────────────────────
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