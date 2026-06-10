package com.blivtech.emptrack.ui.work

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.databinding.ActivitySelectEmployeeBinding
import com.blivtech.emptrack.ui.employee.EmployeeListViewModel
import com.blivtech.emptrack.ui.employee.adapter.EmployeeAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectEmployeeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectEmployeeBinding
    private val viewModel: EmployeeListViewModel by viewModels()
    private lateinit var adapter: EmployeeAdapter

    private val companyId by lazy { intent.getStringExtra("companyId")?:"" }
    private var allEmployees = listOf<EmployeeEntity>()

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
            onCall  = { /* no call in picker */ }
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
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun observeData() {
        viewModel.getEmployees(companyId).observe(this) { employees ->
            allEmployees = employees
            adapter.submitList(employees)
        }
    }

    private fun selectEmployee(employee: EmployeeEntity) {
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