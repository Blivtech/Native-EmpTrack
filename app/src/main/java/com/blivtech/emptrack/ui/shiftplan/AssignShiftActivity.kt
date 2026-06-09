package com.blivtech.emptrack.ui.shiftplan

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.databinding.ActivityAssignShiftBinding
import com.blivtech.emptrack.ui.shiftplan.adapter.AssignShiftAdapter
import com.blivtech.emptrack.utils.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class AssignShiftActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAssignShiftBinding
    private val viewModel: ShiftPlanViewModel by viewModels()
    private lateinit var assignedAdapter: AssignShiftAdapter
    private lateinit var unassignedAdapter: AssignShiftAdapter

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private val companyCode   by lazy { intent.getStringExtra("companyCode") ?: "" }
    private val companyName   by lazy { intent.getStringExtra("companyName") ?: "" }
    private val shiftCode     by lazy { intent.getStringExtra("shiftCode") ?: "" }
    private val shiftName     by lazy { intent.getStringExtra("shiftName") ?: "" }
    private val shiftStart    by lazy { intent.getStringExtra("shiftStartTime") ?: "" }
    private val shiftEnd      by lazy { intent.getStringExtra("shiftEndTime") ?: "" }
    private val weekStartDate by lazy { intent.getStringExtra("weekStartDate") ?: "" }
    private val weekEndDate   by lazy { intent.getStringExtra("weekEndDate") ?: "" }
    private val btCode        by lazy {
        runBlocking { preferenceManager.btCode.first() }
    }

    // ✅ Track assigned vs unassigned
    private val assignedEmployees   = mutableListOf<EmployeeEntity>()
    private val unassignedEmployees = mutableListOf<EmployeeEntity>()
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignShiftBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerViews()
        setupSearch()
        observeData()

        viewModel.loadEmployees(companyCode)
        viewModel.loadAssignedEmpIds(companyCode, shiftCode)
    }

    private fun setupUI() {
        binding.ivBack.setOnClickListener { finish() }
        binding.tvShiftName.text  = "$shiftName · $companyName"
        binding.tvShiftTime.text  = "${shiftStart.take(5)} – ${shiftEnd.take(5)}"
        binding.tvWeekRange.text  = weekStartDate

        binding.btnSave.setOnClickListener {
            viewModel.saveShiftPlan(
                btCode       = btCode,
                companyCode  = companyCode,
                shiftCode    = shiftCode,
                assignedEmployees = assignedEmployees.toList()
            )
        }

        // ✅ Save state
        viewModel.saveState.observe(this) { saved ->
            if (saved) {
                Snackbar.make(
                    binding.root,
                    "Shift plan saved! ✅",
                    Snackbar.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun setupRecyclerViews() {
        // ✅ Assigned list
        assignedAdapter = AssignShiftAdapter(
            onAction = { emp ->
                // ✅ Remove from assigned
                assignedEmployees.remove(emp)
                unassignedEmployees.add(emp)
                refreshLists()
            },
            isAssigned = true
        )
        binding.rvAssigned.layoutManager = LinearLayoutManager(this)
        binding.rvAssigned.adapter = assignedAdapter

        // ✅ Unassigned list
        unassignedAdapter = AssignShiftAdapter(
            onAction = { emp ->
                // ✅ Add to assigned
                unassignedEmployees.remove(emp)
                assignedEmployees.add(emp)
                refreshLists()
            },
            isAssigned = false
        )
        binding.rvUnassigned.layoutManager = LinearLayoutManager(this)
        binding.rvUnassigned.adapter = unassignedAdapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString().trim().lowercase()
                refreshLists()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeData() {

        // ✅ All employees loaded
        viewModel.employees.observe(this) { allEmps ->
            viewModel.assignedEmpIds.observe(this) { assignedIds ->
                assignedEmployees.clear()
                unassignedEmployees.clear()

                allEmps.forEach { emp ->
                    if (emp.empCode in assignedIds) {
                        assignedEmployees.add(emp)
                    } else {
                        unassignedEmployees.add(emp)
                    }
                }
                refreshLists()
            }
        }
    }

    private fun refreshLists() {
        // ✅ Apply search
        val filteredAssigned = if (searchQuery.isEmpty()) {
            assignedEmployees.toList()
        } else {
            assignedEmployees.filter {
                it.name.lowercase().contains(searchQuery) ||
                it.empCode.lowercase().contains(searchQuery)
            }
        }

        val filteredUnassigned = if (searchQuery.isEmpty()) {
            unassignedEmployees.toList()
        } else {
            unassignedEmployees.filter {
                it.name.lowercase().contains(searchQuery) ||
                it.empCode.lowercase().contains(searchQuery)
            }
        }

        assignedAdapter.submitList(filteredAssigned)
        unassignedAdapter.submitList(filteredUnassigned)

        // ✅ Update counts
        binding.tvAssignedCount.text   = "${assignedEmployees.size} employees"
        binding.tvUnassignedCount.text = "${unassignedEmployees.size} remaining"
        binding.tvProgress.text =
            "${assignedEmployees.size}/${assignedEmployees.size + unassignedEmployees.size}"
    }
}