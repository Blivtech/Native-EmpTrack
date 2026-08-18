package com.blivtech.emptrack.ui.shiftplan.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.databinding.ItemAssignShiftBinding

class AssignShiftAdapter(
    private val onAction: (EmployeeEntity) -> Unit,
    private val isAssigned: Boolean
) : RecyclerView.Adapter<AssignShiftAdapter.ViewHolder>() {

    private var employees = listOf<EmployeeEntity>()

    fun submitList(list: List<EmployeeEntity>) {
        employees = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        val binding: ItemAssignShiftBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(emp: EmployeeEntity) {
            val initials = emp.name.split(" ")
                .filter { it.isNotBlank() }
                .take(2).joinToString("") { it.first().uppercase() }

            binding.tvInitials.text = initials
            binding.tvEmpName.text  = emp.name
            binding.tvEmpCode.text  = emp.empCode

            if (isAssigned) {
                // assigned → green card + soft-red remove (✕)
                binding.root.setBackgroundResource(R.drawable.bg_card_working)
                binding.btnAction.setBackgroundResource(R.drawable.bg_circle_red)
                binding.btnAction.setImageResource(R.drawable.ic_close)
                binding.btnAction.setColorFilter(Color.parseColor("#DC2626"))
            } else {
                // unassigned → dashed card + soft-blue add (+)
                binding.root.setBackgroundResource(R.drawable.bg_card_unselected)
                binding.btnAction.setBackgroundResource(R.drawable.bg_circle_blue)
                binding.btnAction.setImageResource(R.drawable.ic_add)
                binding.btnAction.setColorFilter(Color.parseColor("#2563EB"))
            }

            binding.btnAction.setOnClickListener { onAction(emp) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemAssignShiftBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(employees[position])

    override fun getItemCount() = employees.size
}
