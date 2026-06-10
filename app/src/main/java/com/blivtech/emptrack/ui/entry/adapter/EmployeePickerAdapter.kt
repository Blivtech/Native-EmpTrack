package com.blivtech.emptrack.ui.entry.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.databinding.ItemEmployeePickerBinding

class EmployeePickerAdapter(
    private val isSingle: Boolean,
    private val selectedCodes: MutableSet<String>,
    private val onSelect: (EmployeeEntity) -> Unit
) : RecyclerView.Adapter<EmployeePickerAdapter.ViewHolder>() {

    private var employees = listOf<EmployeeEntity>()

    fun submitList(list: List<EmployeeEntity>) {
        employees = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        val binding: ItemEmployeePickerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(emp: EmployeeEntity) {
            val initials = emp.name.split(" ")
                .take(2).joinToString("") { it.first().uppercase() }
            binding.tvInitials.text = initials
            binding.tvEmpName.text  = emp.name
            binding.tvEmpCode.text  = emp.empCode

            val isSelected = emp.empCode in selectedCodes

            // ✅ Show checkbox for multiple, hide for single
            binding.cbSelect.visibility =
                if (isSingle) android.view.View.GONE else android.view.View.VISIBLE
            binding.cbSelect.isChecked = isSelected

            binding.root.setBackgroundResource(
                if (isSelected) R.drawable.bg_card_working
                else R.drawable.bg_card_unselected
            )

            binding.root.setOnClickListener {
                if (!isSingle) {
                    if (isSelected) selectedCodes.remove(emp.empCode)
                    else selectedCodes.add(emp.empCode)
                    notifyItemChanged(adapterPosition)
                }
                onSelect(emp)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemEmployeePickerBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(employees[position])

    override fun getItemCount() = employees.size
}