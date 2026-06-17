package com.blivtech.emptrack.ui.shiftplan

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.databinding.ActivityShiftPlanBinding
import com.blivtech.emptrack.utils.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShiftPlanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShiftPlanBinding
    private val viewModel: ShiftPlanViewModel by viewModels()
    private lateinit var shiftAdapter: ShiftPlanAdapter

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var companyName = ""
    private var companyCode = ""

    // ─────────────────────────────────────────────────────────────────────
    // onCreate
    // ─────────────────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShiftPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            companyCode = preferenceManager.selectedCompanyCode.first()
            companyName = preferenceManager.selectedCompanyName.first()

            setupAdapter()
            setupUI()
            observeData()

            // Initial data load
            viewModel.loadEmployees(companyCode)
            viewModel.loadWeekPlan(companyCode)
            viewModel.checkLastWeekPlan(companyCode)
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // FIX 3: onResume now also triggers a counts refresh on the adapter
    //         so emp counts are correct after returning from AssignShiftActivity.
    // ─────────────────────────────────────────────────────────────────────
    override fun onResume() {
        super.onResume()
        if (companyCode.isNotEmpty()) {
            viewModel.loadWeekPlan(companyCode)
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Adapter setup
    // ─────────────────────────────────────────────────────────────────────
    private fun setupAdapter() {
        shiftAdapter = ShiftPlanAdapter(
            onAssignClick = { shift -> openAssignScreen(shift) },
            getEmpCount   = { shiftCode -> viewModel.getShiftEmpCount(shiftCode) },
            getEmpNames   = { shiftCode -> viewModel.getShiftEmpNames(shiftCode) }
        )

        binding.rvShiftCards.apply {
            layoutManager = LinearLayoutManager(this@ShiftPlanActivity)
            adapter = shiftAdapter
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // UI wiring
    // ─────────────────────────────────────────────────────────────────────
    private fun setupUI() {
        binding.ivBack.setOnClickListener { finish() }
        binding.tvCompanyName.text = companyName

        // Week label
     //x`   updateWeekDisplay()

//        // FIX 4: week navigation arrows actually work now
//        binding.ivPrevWeek.setOnClickListener {
//            viewModel.previousWeek()
//            updateWeekDisplay()
//        }
//
//        binding.ivNextWeek.setOnClickListener {
//            viewModel.nextWeek()
//            updateWeekDisplay()
//        }
//
//        // Copy banner
//        binding.ivCopyPlan.setOnClickListener { showCopyDialog() }
//
//        // Copy-banner close (optional — hide without copying)
//        binding.ivCloseBanner.setOnClickListener {
//            binding.layoutCopyBanner.visibility = View.GONE
//        }
//    }
//
//    private fun updateWeekDisplay() {
//        binding.tvWeekLabel.text  = viewModel.getWeekLabel()
//        binding.tvWeekNumber.text = viewModel.getWeekNumber()
//
//        // Dim "next" arrow when already on current week (optional UX)
//        binding.ivNextWeek.alpha = if (viewModel.isCurrentWeek()) 0.4f else 1f
//    }
    }
    // ─────────────────────────────────────────────────────────────────────
    // Observers
    // ─────────────────────────────────────────────────────────────────────
    private fun observeData() {

        // Shift list → submit to adapter
        viewModel.getShifts(companyCode).observe(this) { shiftList ->
            shiftAdapter.submitList(shiftList)
            updateUnassignedWarning()
        }

        // FIX 1 & 5: weekPlan changes → refresh emp count cells AND warning
        viewModel.weekPlan.observe(this) {
            shiftAdapter.refreshCounts()       // ← KEY FIX: was completely missing
            updateUnassignedWarning()
        }

        // Employee list → refresh warning banner
        viewModel.employees.observe(this) {
            shiftAdapter.refreshCounts()
            updateUnassignedWarning()
        }

        // Last-week copy banner
        viewModel.hasLastWeekPlan.observe(this) { hasLastWeek ->
            val weekPlanEmpty = viewModel.weekPlan.value.isNullOrEmpty()
            binding.layoutCopyBanner.visibility =
                if (hasLastWeek && weekPlanEmpty) View.VISIBLE else View.GONE
        }

        // FIX 6: saveState was never observed — now we reload after copy
        viewModel.saveState.observe(this) { success ->
            if (success) {
                viewModel.loadWeekPlan(companyCode)
                viewModel.resetSaveState()
                binding.layoutCopyBanner.visibility = View.GONE
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Unassigned warning
    // ─────────────────────────────────────────────────────────────────────
    private fun updateUnassignedWarning() {
        val unassigned = viewModel.getUnassignedCount()
        if (unassigned > 0) {
            binding.layoutUnassignedWarning.visibility = View.VISIBLE
            binding.tvUnassignedCount.text =
                "$unassigned employee${if (unassigned > 1) "s" else ""} not assigned to any shift this week"
        } else {
            binding.layoutUnassignedWarning.visibility = View.GONE
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Dialogs / Navigation
    // ─────────────────────────────────────────────────────────────────────
    private fun showCopyDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Copy last week plan?")
            .setMessage("This will copy all shift assignments from last week to this week.")
            .setPositiveButton("Copy") { _, _ ->
                viewModel.copyLastWeekPlan(companyCode)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openAssignScreen(shift: ShiftEntity) {
        startActivity(
            Intent(this, AssignShiftActivity::class.java).apply {
                putExtra("companyCode",      companyCode)
                putExtra("companyName",      companyName)
                putExtra("shiftCode",        shift.shiftCode)
                putExtra("shiftName",        shift.shiftName)
                putExtra("shiftStartTime",   shift.startTime.toString())
                putExtra("shiftEndTime",     shift.endTime.toString())
                putExtra("weekStartDate",    viewModel.getSelectedWeekStart())
                putExtra("weekEndDate",      viewModel.getSelectedWeekEnd())
            }
        )
    }
}