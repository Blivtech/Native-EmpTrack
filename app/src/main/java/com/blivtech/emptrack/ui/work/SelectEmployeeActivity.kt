package com.blivtech.emptrack.ui.work

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.blivtech.emptrack.data.model.EmployeeWithDetails
import com.blivtech.emptrack.databinding.ActivitySelectEmployeeBinding
import com.blivtech.emptrack.ui.employee.EmployeeListViewModel
import com.blivtech.emptrack.ui.employee.adapter.EmployeeAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectEmployeeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectEmployeeBinding
    private val viewModel: EmployeeListViewModel by viewModels()
    private lateinit var adapter: EmployeeAdapter

    // ✅ Read the same key the caller sends ("companyCode"), named correctly
    private val companyCode by lazy { intent.getStringExtra("companyCode") ?: "" }
    private var allEmployees = listOf<EmployeeWithDetails>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectEmployeeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }
        binding.tvTitle.text = "Select Employee"
        binding.tvSubtitle.text = "Tap to select"

        setupRecyclerView()
        setupSearch()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = EmployeeAdapter(
            onClick = { employee -> selectEmployee(employee) },
            onCall = { /* no call in picker */ }
        )
        binding.rvEmployees.apply {
            layoutManager = LinearLayoutManager(this@SelectEmployeeActivity)
            adapter = this@SelectEmployeeActivity.adapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                val filtered = if (query.isEmpty()) allEmployees
                else allEmployees.filter {
                    it.name.lowercase().contains(query) ||
                            it.empCode.lowercase().contains(query)
                }
                adapter.submitList(filtered)
                toggleEmptyState(filtered.isEmpty())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun observeData() {
        viewModel.getEmployeess(companyCode).observe(this) { employees ->
            allEmployees = employees
            adapter.submitList(employees)
            toggleEmptyState(employees.isEmpty())
        }
    }

    // ✅ Show/hide the empty-state view that already exists in the layout
    private fun toggleEmptyState(isEmpty: Boolean) {
        binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvEmployees.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun selectEmployee(employee: EmployeeWithDetails) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra("empCode", employee.empCode)
                putExtra("empName", employee.name)
            }
        )
        finish()
    }
}