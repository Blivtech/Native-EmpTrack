package com.blivtech.emptrack.ui.attendance.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.databinding.ItemDeviationEmployeeBinding

class DeviationAdapter(
    private val onSelectionChanged: (empCode: String, isSelected: Boolean) -> Unit
) : RecyclerView.Adapter<DeviationAdapter.ViewHolder>() {

    private var employees     = listOf<EmployeeEntity>()
    private val selectedCodes = mutableSetOf<String>()

    fun submitList(list: List<EmployeeEntity>) {
        employees = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        val binding: ItemDeviationEmployeeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(emp: EmployeeEntity) {
            val initials = emp.name.split(" ")
                .take(2).joinToString("") { it.first().uppercase() }
            binding.tvInitials.text = initials
            binding.tvEmpName.text  = emp.name
            binding.tvEmpCode.text  = emp.empCode

            val isSelected = emp.empCode in selectedCodes
            binding.cbSelect.isChecked = isSelected
            binding.root.setBackgroundResource(
                if (isSelected) R.drawable.bg_card_working
                else R.drawable.bg_card_unselected
            )

            binding.root.setOnClickListener {
                val newState = emp.empCode !in selectedCodes
                if (newState) selectedCodes.add(emp.empCode)
                else selectedCodes.remove(emp.empCode)
                notifyItemChanged(adapterPosition)
                onSelectionChanged(emp.empCode, newState)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemDeviationEmployeeBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(employees[position])

    override fun getItemCount() = employees.size
}