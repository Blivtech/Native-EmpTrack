package com.blivtech.emptrack.ui.employee.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.databinding.ItemEmployeeBinding
import com.blivtech.emptrack.data.local.entity.DesignationEntity
class EmployeeAdapter(

    private val onClick: (EmployeeEntity) -> Unit,
    private val onCall: (EmployeeEntity) -> Unit
) : ListAdapter<EmployeeEntity, EmployeeAdapter.ViewHolder>(DiffCallback()) {

    private var designationList: List<DesignationEntity> = emptyList()


    inner class ViewHolder(val binding: ItemEmployeeBinding) :
        RecyclerView.ViewHolder(binding.root) {




        fun bind(employee: EmployeeEntity) {
            // ✅ Initials
            val initials = employee.name.split(" ")
                .take(2).joinToString("") { it.first().uppercase() }
            binding.tvInitials.text = initials

            // ✅ Info
            binding.tvEmpName.text = employee.name
            binding.tvEmpRole.text = "EMP · ${employee.empCode}"

            val designationName = designationList.firstOrNull {
                it.id.toString() == employee.desgCode ||
                        it.desgCode == employee.desgCode
            }?.name ?: employee.desgCode

            binding.tvEmpDept.text = designationName
            binding.tvEmpDept.text = "Size = ${designationList.size}"

            // ✅ Status badge
            binding.tvStatus.text = if (employee.status == 1) "Active" else "Inactive"
            binding.tvStatus.setBackgroundResource(
                if (employee.status == 1)
                    com.blivtech.emptrack.R.drawable.bg_badge_green
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

    class DiffCallback : DiffUtil.ItemCallback<EmployeeEntity>() {
        override fun areItemsTheSame(a: EmployeeEntity, b: EmployeeEntity) = a.id == b.id
        override fun areContentsTheSame(a: EmployeeEntity, b: EmployeeEntity) = a == b
    }

    fun setDesignationList(list: List<DesignationEntity>) {
        designationList = list
        Log.d("ADAPTER", "Designation size = ${list.size}")
        notifyDataSetChanged()
    }


}