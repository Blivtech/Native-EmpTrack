package com.blivtech.emptrack.ui.report.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.data.model.WeeklyEmployeeSummaryDto
import com.blivtech.emptrack.databinding.ItemWeeklyReportRowBinding

class WeeklyReportAdapter(
    private val onCount: (WeeklyEmployeeSummaryDto, String) -> Unit
) : RecyclerView.Adapter<WeeklyReportAdapter.VH>() {

    private var items = listOf<WeeklyEmployeeSummaryDto>()

    fun submitList(list: List<WeeklyEmployeeSummaryDto>) {
        items = list
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemWeeklyReportRowBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(emp: WeeklyEmployeeSummaryDto) {
            b.tvEmpName.text = emp.empName
            b.tvEmpCode.text = emp.empCode

            b.tvPresent.text = emp.presentDays.toString()
            b.tvAbsent.text  = emp.absentDays.toString()
            b.tvHoliday.text = emp.holidayDays.toString()
            b.tvWeekOff.text = emp.weekOffDays.toString()
            b.tvTotal.text   = emp.totalDays.toString()

            b.tvPresent.setOnClickListener { onCount(emp, "PRESENT") }
            b.tvAbsent.setOnClickListener  { onCount(emp, "ABSENT") }
            b.tvHoliday.setOnClickListener { onCount(emp, "HOLIDAY") }
            b.tvWeekOff.setOnClickListener { onCount(emp, "WEEKOFF") }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemWeeklyReportRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size
}