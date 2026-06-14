package com.blivtech.emptrack.ui.company

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.blivtech.emptrack.data.local.entity.CompanyEntity
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.databinding.ActivityCompanyListBinding
import com.blivtech.emptrack.ui.company.adapter.CompanyAdapter
import com.blivtech.emptrack.utils.Resource
import com.blivtech.emptrack.utils.EntityExtensions.toParcel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CompanyListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompanyListBinding
    private val viewModel: CompanyListViewModel by viewModels()
    private lateinit var adapter: CompanyAdapter

    private val btCode by lazy { intent.getStringExtra("btCode") ?: "BT0017" }

    // ✅ Hold all shifts in memory for display
    private val allShifts = mutableMapOf<String, List<ShiftEntity>>()
    private var companies = listOf<CompanyEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanyListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = CompanyAdapter(
            selectedCompanyId = viewModel.getSelectedCompanyId(),
            onSelect = { company -> handleSelect(company) },
            onEdit = { company -> openEditCompany(company) },
            onDelete = { company -> confirmDelete(company) }
        )
        binding.rvCompanies.apply {
            layoutManager = LinearLayoutManager(this@CompanyListActivity)
            adapter = this@CompanyListActivity.adapter
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.btnAddCompany.setOnClickListener {
            startActivity(
                Intent(this, AddEditCompanyActivity::class.java).apply {
                    putExtra("btCode", btCode)
                }
            )
        }
    }

    private fun observeData() {
        // ✅ Observe companies
        viewModel.getCompanies(btCode).observe(this) { list ->
            companies = list
            binding.layoutEmpty.visibility =
                if (list.isEmpty()) View.VISIBLE else View.GONE
            binding.rvCompanies.visibility =
                if (list.isEmpty()) View.GONE else View.VISIBLE

            // ✅ Load shifts for each company
            list.forEach { company ->
                viewModel.getShifts(company.companyCode).observe(this) { shifts ->
                    allShifts[company.companyCode] = shifts
                    refreshAdapter()
                }
            }
            refreshAdapter()
        }

        // ✅ Observe delete state
        viewModel.deleteState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    Snackbar.make(binding.root, "Deleting...", Snackbar.LENGTH_SHORT).show()
                }
                is Resource.Success -> {
                    Snackbar.make(binding.root, "Company deleted!", Snackbar.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    Snackbar.make(binding.root, resource.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        // ✅ Observe selected company — pass back to HomeActivity
        viewModel.selectedCompany.observe(this) { company ->
            company?.let {
                val resultIntent = Intent().apply {
                    putExtra("selectedCompanyId", it.id)
                    putExtra("selectedCompanyName", it.name)
                    putExtra("selectedCompanyCode", it.companyCode)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    private fun refreshAdapter() {
        val items = companies.map { company ->
            CompanyAdapter.CompanyWithShifts(
                company = company,
                shifts = allShifts[company.companyCode] ?: emptyList()
            )
        }
        adapter.submitList(items)
    }

    private fun handleSelect(company: CompanyEntity) {
        viewModel.selectCompany(company)
    }

    private fun openEditCompany(company: CompanyEntity) {
        val shifts = allShifts[company.companyCode] ?: emptyList()

        startActivity(
            Intent(this, AddEditCompanyActivity::class.java).apply {
                putExtra("btCode", btCode)
                putExtra("companyCode", company.companyCode)
                putExtra("company", company.toParcel())
                putParcelableArrayListExtra(
                    "shifts",
                    ArrayList(shifts.map { it.toParcel() })
                )
            }
        )
    }

    private fun confirmDelete(company: CompanyEntity) {
        AlertDialog.Builder(this)
            .setTitle("Delete company")
            .setMessage("Are you sure you want to delete ${company.name}? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteCompany(company, btCode)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}