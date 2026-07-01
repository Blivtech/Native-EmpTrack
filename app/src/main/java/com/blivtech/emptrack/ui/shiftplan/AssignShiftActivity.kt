package com.blivtech.emptrack.ui.shiftplan

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.databinding.ActivityAssignShiftBinding
import com.blivtech.emptrack.ui.shiftplan.adapter.AssignShiftAdapter
import com.blivtech.emptrack.utils.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AssignShiftActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAssignShiftBinding
    private val viewModel: ShiftPlanViewModel by viewModels()

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private lateinit var assignedAdapter: AssignShiftAdapter
    private lateinit var unassignedAdapter: AssignShiftAdapter

    private var btCode = ""
    private val companyCode    by lazy { intent.getStringExtra("companyCode")    ?: "" }
    private val companyName    by lazy { intent.getStringExtra("companyName")    ?: "" }
    private val shiftCode      by lazy { intent.getStringExtra("shiftCode")      ?: "" }
    private val shiftName      by lazy { intent.getStringExtra("shiftName")      ?: "" }
    private val shiftStartTime by lazy { intent.getStringExtra("shiftStartTime") ?: "" }
    private val shiftEndTime   by lazy { intent.getStringExtra("shiftEndTime")   ?: "" }

    // ✅ Working copies — admin edits these, Save persists them
    private val assignedEmployees   = mutableListOf<EmployeeEntity>()
    private val unassignedEmployees = mutableListOf<EmployeeEntity>()

    // ✅ Search state
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignShiftBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            btCode = preferenceManager.btCode.first()

            setupUI()
            setupRecyclerViews()
            setupSearch()
            observeData()

            // ✅ Load BOTH employees and the FULL week plan (all shifts)
            viewModel.loadEmployees(companyCode)
            viewModel.loadWeekPlan(companyCode)
        }
    }

    // ─────────────────────────────────
    // ✅ Setup
    // ─────────────────────────────────
    private fun setupUI() {
        binding.ivBack.setOnClickListener { finish() }
        binding.tvShiftName.text  = shiftName
        binding.tvShiftTime.text  =
            "${shiftStartTime.take(5)} – ${shiftEndTime.take(5)}"
        binding.tvWeekRange.text  = "${viewModel.getWeekLabel()} · $companyName"

        binding.btnSave.setOnClickListener { saveAssignment() }
    }

    private fun setupRecyclerViews() {
        // ✅ Assigned list — tap minus → unassign (moves to unassigned)
        assignedAdapter = AssignShiftAdapter(
            isAssigned = true,
            onAction   = { emp -> unassignEmployee(emp) }
        )
        binding.rvAssigned.apply {
            layoutManager = LinearLayoutManager(this@AssignShiftActivity)
            adapter       = assignedAdapter
        }

        // ✅ Unassigned list — tap plus → assign (moves to assigned)
        unassignedAdapter = AssignShiftAdapter(
            isAssigned = false,
            onAction   = { emp -> assignEmployee(emp) }
        )
        binding.rvUnassigned.apply {
            layoutManager = LinearLayoutManager(this@AssignShiftActivity)
            adapter       = unassignedAdapter
        }
    }

    // ─────────────────────────────────
    // ✅ Search — filters unassigned list only
    // (assigned list stays fully visible regardless of search)
    // ─────────────────────────────────
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString().trim()
                binding.ivClearSearch.visibility =
                    if (searchQuery.isNotEmpty()) View.VISIBLE else View.GONE
                refreshLists()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.ivClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }
    }

    // ─────────────────────────────────
    // ✅ Observe — rebuild BOTH lists whenever
    // employees OR weekPlan change
    // ─────────────────────────────────
    private fun observeData() {
        viewModel.employees.observe(this) { rebuildLists() }
        viewModel.weekPlan.observe(this)  { rebuildLists() }
    }

    /**
     * ★ Single source of truth ★
     * Always derives both lists fresh from ViewModel.
     *  - assignedEmployees   → employees on THIS shift, this week
     *  - unassignedEmployees → employees NOT on ANY shift, this week
     *    (so anyone already on Shift A never appears here for Shift B)
     */
    private fun rebuildLists() {
        assignedEmployees.clear()
        assignedEmployees.addAll(viewModel.getAssignedEmployees(shiftCode))

        unassignedEmployees.clear()
        unassignedEmployees.addAll(viewModel.getAvailableEmployees(shiftCode))

        refreshLists()
    }

    private fun refreshLists() {
        // ✅ Apply search filter to unassigned list only
        val filteredUnassigned = if (searchQuery.isEmpty()) {
            unassignedEmployees
        } else {
            unassignedEmployees.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.empCode.contains(searchQuery, ignoreCase = true)
            }
        }

        assignedAdapter.submitList(assignedEmployees)
        unassignedAdapter.submitList(filteredUnassigned)

        binding.tvAssignedCount.text   = assignedEmployees.size.toString()
        binding.tvUnassignedCount.text = unassignedEmployees.size.toString()
        binding.tvProgress.text =
            "${assignedEmployees.size}/${assignedEmployees.size + unassignedEmployees.size}"
    }

    // ─────────────────────────────────
    // ✅ Assign — move from unassigned → assigned (in-memory, until Save)
    // ─────────────────────────────────
    private fun assignEmployee(emp: EmployeeEntity) {
        if (assignedEmployees.none { it.empCode == emp.empCode }) {
            assignedEmployees.add(emp)
        }
        unassignedEmployees.removeAll { it.empCode == emp.empCode }
        refreshLists()
    }

    // ─────────────────────────────────
    // ✅ Unassign — move from assigned → unassigned (in-memory, until Save)
    // ─────────────────────────────────
    private fun unassignEmployee(emp: EmployeeEntity) {
        assignedEmployees.removeAll { it.empCode == emp.empCode }
        if (unassignedEmployees.none { it.empCode == emp.empCode }) {
            unassignedEmployees.add(emp)
        }
        refreshLists()
    }

    // ─────────────────────────────────
    // ✅ Save — persists the final assignedEmployees list for THIS shift only.
    // Repository REPLACES this shift's rows; other shifts' rows untouched.
    // ─────────────────────────────────
    private fun saveAssignment() {
        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Saving..."

        viewModel.saveShiftPlan(
            btCode            = btCode,
            companyCode       = companyCode,
            shiftCode         = shiftCode,
            assignedEmployees = assignedEmployees
        )

        viewModel.saveState.observe(this) { saved ->
            if (saved) {
                Snackbar.make(
                    binding.root,
                    "Shift plan saved ✅",
                    Snackbar.LENGTH_SHORT
                ).show()
                viewModel.resetSaveState()
                finish()
            }
        }
    }
}