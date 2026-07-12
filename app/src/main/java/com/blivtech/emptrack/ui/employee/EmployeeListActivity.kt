package com.blivtech.emptrack.ui.employee

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.databinding.ActivityEmployeeListBinding
import com.blivtech.emptrack.ui.employee.adapter.EmployeeAdapter
import com.blivtech.emptrack.utils.PreferenceManager
import com.blivtech.emptrack.utils.Resource
import com.blivtech.emptrack.data.local.entity.DesignationEntity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EmployeeListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmployeeListBinding
    private val viewModel: EmployeeListViewModel by viewModels()
    private lateinit var adapter: EmployeeAdapter
    private var designationList = listOf<DesignationEntity>()

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var companyCode  = ""
    private var companyName = ""
    private var btCode = ""

    private var allEmployees = listOf<EmployeeEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()
        setupSearch()

        lifecycleScope.launch {
            btCode = preferenceManager.btCode.first()
            companyCode = preferenceManager.selectedCompanyCode.first()
            companyName = preferenceManager.selectedCompanyName.first()

            binding.tvTitle.text = "Employees"
            binding.tvSubtitle.text = companyName

            Log.d("companyCodecompanyCodeonCreate", "onCreate: $companyCode")

            observeData()
        }
    }



    private fun setupRecyclerView() {
        adapter = EmployeeAdapter(
            onClick = { employee -> openDetail(employee) },
            onCall  = { employee -> callEmployee(employee) }
        )
        binding.rvEmployees.apply {
            layoutManager = LinearLayoutManager(this@EmployeeListActivity)
            adapter = this@EmployeeListActivity.adapter
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.btnAdd.setOnClickListener {
            startActivity(
                Intent(this, AddEditEmployeeActivity::class.java)
            )
        }

        binding.chipAll.setOnCheckedChangeListener { _, checked ->
            if (checked) filterEmployees("all")
        }
        binding.chipActive.setOnCheckedChangeListener { _, checked ->
            if (checked) filterEmployees("active")
        }
        binding.chipInactive.setOnCheckedChangeListener { _, checked ->
            if (checked) filterEmployees("inactive")
        }
        binding.chipMale.setOnCheckedChangeListener { _, checked ->
            if (checked) filterEmployees("male")
        }
        binding.chipFemale.setOnCheckedChangeListener { _, checked ->
            if (checked) filterEmployees("female")
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                val filtered = if (query.isEmpty()) allEmployees
                else allEmployees.filter {
                    it.name.lowercase().contains(query) ||
                    it.empCode.lowercase().contains(query) ||
                    it.phone?.contains(query) == true
                }
                adapter.submitList(filtered)
                updateEmptyState(filtered.isEmpty())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun observeData() {
        Log.d("companyCodecompanyCode", "observeData: $companyCode")
        viewModel.getDesignations(btCode).observe(this) { list ->

            Log.e("AAAA", "Designation Size = ${list.size}")

            list.forEach {
                Log.e("AAAA", "Code=${it.desgCode} Name=${it.name}")
            }

            adapter.setDesignationList(list)
        }
        viewModel.getEmployees(companyCode).observe(this) { employees ->
            Log.d("employeesemployeesemployees", "observeData: $employees")
            allEmployees = employees
            binding.tvSubtitle.text = "$companyName · ${employees.size} employees"
            adapter.submitList(employees)
            updateEmptyState(employees.isEmpty())
        }

        viewModel.deleteState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {}
                is Resource.Success ->
                    Snackbar.make(binding.root, "Employee deleted!", Snackbar.LENGTH_SHORT).show()
                is Resource.Error ->
                    Snackbar.make(binding.root, resource.message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun filterEmployees(filter: String) {
        val filtered = when (filter) {
            "active"   -> allEmployees.filter { it.status == 1 }
            "inactive" -> allEmployees.filter { it.status == 0 }
            "male"     -> allEmployees.filter { it.gender == 1 }
            "female"   -> allEmployees.filter { it.gender == 2 }
            else       -> allEmployees
        }
        adapter.submitList(filtered)
        updateEmptyState(filtered.isEmpty())
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvEmployees.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun openDetail(employee: EmployeeEntity) {
        startActivity(
            Intent(this, EmployeeDetailActivity::class.java).apply {
                putExtra("empCode", employee.empCode)
                putExtra("btCode", btCode)
            }
        )
    }

    private fun callEmployee(employee: EmployeeEntity) {
        employee.phone?.let { phone ->
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
        } ?: Snackbar.make(binding.root, "No phone number!", Snackbar.LENGTH_SHORT).show()
    }
}