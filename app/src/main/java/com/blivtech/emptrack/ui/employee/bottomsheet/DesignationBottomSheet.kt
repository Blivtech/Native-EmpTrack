package com.blivtech.emptrack.ui.employee.bottomsheet

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.blivtech.emptrack.data.local.entity.DesignationEntity
import com.blivtech.emptrack.databinding.BottomsheetDepartmentBinding
import com.blivtech.emptrack.ui.employee.adapter.DesignationSheetAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DesignationBottomSheet(
    private val designations: List<DesignationEntity>,
    private val selectedId: Long? = null,
    private val onSelected: (DesignationEntity) -> Unit,
    private val onAddNew: (name: String, desc: String) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomsheetDepartmentBinding
    private lateinit var adapter: DesignationSheetAdapter
    private var filteredList = designations.toMutableList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomsheetDepartmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Change title
        binding.root.findViewById<android.widget.TextView>(
            com.blivtech.emptrack.R.id.tvSheetTitle
        )?.text = "Select designation"

        setupRecyclerView()
        setupSearch()
        setupAddNew()
    }

    private fun setupRecyclerView() {
        adapter = DesignationSheetAdapter(selectedId) { desg ->
            onSelected(desg)
            dismiss()
        }
        binding.rvDepartments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDepartments.adapter = adapter
        adapter.submitList(filteredList.toList())
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                filteredList = if (query.isEmpty()) designations.toMutableList()
                else designations.filter { it.name.lowercase().contains(query) }.toMutableList()
                adapter.submitList(filteredList.toList())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupAddNew() {
        binding.btnAddDept.setOnClickListener {
            binding.layoutAddForm.visibility =
                if (binding.layoutAddForm.visibility == View.GONE) View.VISIBLE
                else View.GONE
        }

        binding.btnCancelAdd.setOnClickListener {
            binding.layoutAddForm.visibility = View.GONE
            binding.etDeptName.text?.clear()
            binding.etDeptDesc.text?.clear()
        }

        binding.btnSaveAdd.setOnClickListener {
            val name = binding.etDeptName.text.toString().trim()
            val desc = binding.etDeptDesc.text.toString().trim()
            if (name.isEmpty()) {
                binding.tilDeptName.error = "Name is required"
                return@setOnClickListener
            }
            binding.tilDeptName.error = null
            onAddNew(name, desc)
            dismiss()
        }
    }
}