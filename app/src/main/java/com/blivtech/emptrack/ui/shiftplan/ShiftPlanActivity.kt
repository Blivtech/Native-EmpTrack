package com.blivtech.emptrack.ui.shiftplan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.data.local.entity.ShiftPlanEntity
import com.blivtech.emptrack.databinding.ActivityShiftPlanBinding
import com.blivtech.emptrack.utils.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class ShiftPlanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShiftPlanBinding
    private val viewModel: ShiftPlanViewModel by viewModels()

    @Inject
    lateinit var preferenceManager: PreferenceManager

    var companyName = ""
    var companyCode = ""



    private var shifts = listOf<ShiftEntity>()
    private var weekPlan = listOf<ShiftPlanEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShiftPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            companyCode = preferenceManager.selectedCompanyCode.first()
            companyName = preferenceManager.selectedCompanyName.first()

            setupUI()
            observeData()

            viewModel.loadEmployees(companyCode)
            viewModel.loadWeekPlan(companyCode)
            viewModel.checkLastWeekPlan(companyCode)
        }
    }

    override fun onResume() {
        super.onResume()
        if (companyCode.isNotEmpty()) {
            viewModel.loadWeekPlan(companyCode)
        }
    }

    private fun setupUI() {
        binding.ivBack.setOnClickListener { finish() }
        binding.tvCompanyName.text = companyName
        updateWeekDisplay()

        // ✅ Week navigation
        binding.btnPrevWeek.setOnClickListener {
            viewModel.previousWeek()
            updateWeekDisplay()
        }
        binding.btnNextWeek.setOnClickListener {
            viewModel.nextWeek()
            updateWeekDisplay()
        }

        // ✅ Copy last week
        binding.ivCopyPlan.setOnClickListener {
            showCopyDialog()
        }
    }
    private fun updateWeekDisplay() {
        // ✅ Week range label — "8 Jun – 14 Jun 2026"
        binding.tvWeekRange.text = viewModel.getWeekLabel()

        // ✅ Week number — "Week 23 · 2026"
        binding.tvWeekNumber.text = viewModel.getWeekNumber()

        // ✅ Current week badge
        binding.tvCurrentBadge.visibility =
            if (viewModel.isCurrentWeek()) View.VISIBLE else View.GONE

        // ✅ Disable next week button if current week
        binding.btnNextWeek.alpha =
            if (viewModel.isCurrentWeek()) 0.4f else 1.0f
        binding.btnNextWeek.isEnabled = !viewModel.isCurrentWeek()

        // ✅ Reload week plan for selected week
        viewModel.loadWeekPlan(companyCode)

        // ✅ Check last week plan for copy banner
        viewModel.checkLastWeekPlan(companyCode)
    }

    private fun observeData() {

        // ✅ Load shifts dynamically
        viewModel.getShifts(companyCode).observe(this) { shiftList ->
            shifts = shiftList
            updateShiftCards()         // ✅ Rebuild cards
        }

        // ✅ Refresh cards when week plan loads
        viewModel.weekPlan.observe(this) { plan ->
            weekPlan = plan
            updateShiftCards()         // ✅ Refresh counts
            updateUnassignedWarning()
        }

        // ✅ Refresh employee data
        viewModel.employees.observe(this) {
            updateShiftCards()         // ✅ Refresh names
            updateUnassignedWarning()
        }

        // ✅ Last week plan check
        viewModel.hasLastWeekPlan.observe(this) { hasLastWeek ->
            binding.layoutCopyBanner.visibility =
                if (hasLastWeek && weekPlan.isEmpty())
                    View.VISIBLE else View.GONE
        }
    }

    private fun updateShiftCards() {
        // ✅ Remove all existing shift cards first
        binding.layoutShiftCards.removeAllViews()

        val allEmployees = viewModel.employees.value ?: emptyList()

        shifts.forEach { shift ->
            val empCount = viewModel.getShiftEmpCount(shift.shiftCode)
            val shiftEmpNames = viewModel.getShiftEmpNames(shift.shiftCode)

            // ✅ Inflate shift card dynamically
            val cardView = layoutInflater.inflate(
                R.layout.item_shift_plan_card,
                binding.layoutShiftCards,
                false
            )

            // ✅ Bind data
            cardView.findViewById<TextView>(R.id.tvShiftName).text = shift.shiftName
            cardView.findViewById<TextView>(R.id.tvShiftTime).text =
                "${shift.startTime.toString().take(5)} – ${shift.endTime.toString().take(5)}"
            cardView.findViewById<TextView>(R.id.tvShiftEmpCount).text =
                "$empCount emp"
            cardView.findViewById<TextView>(R.id.tvShiftEmployees).text =
                if (shiftEmpNames.isNotEmpty())
                    shiftEmpNames.joinToString(", ") +
                            if (empCount > 4) " +${empCount - 4} more" else ""
                else "No employees assigned"

            // ✅ Button
            val btnAssign = cardView.findViewById<Button>(R.id.btnAssign)
            btnAssign.text = if (empCount > 0) "Edit plan" else "Assign now"
            btnAssign.setOnClickListener {
                openAssignScreen(shift)
            }

            // ✅ Unassigned alert
            val layoutAlert = cardView.findViewById<LinearLayout>(R.id.layoutAlert)
            layoutAlert.visibility = if (empCount == 0) View.VISIBLE else View.GONE

            // ✅ Shift icon color based on index
            val shiftIndex = shifts.indexOf(shift)
            val iconBg = cardView.findViewById<LinearLayout>(R.id.layoutShiftIcon)
            val tvShiftIndex = cardView.findViewById<TextView>(R.id.tvShiftIndex)
            tvShiftIndex.text = "Shift ${shiftIndex + 1}"

            when (shiftIndex % 3) {
                0 -> iconBg.setBackgroundResource(R.drawable.bg_shift_morning)
                1 -> iconBg.setBackgroundResource(R.drawable.bg_shift_evening)
                2 -> iconBg.setBackgroundResource(R.drawable.bg_shift_night)
            }

            // ✅ Add card to layout
            binding.layoutShiftCards.addView(cardView)
        }
    }
    private fun updateUnassignedWarning() {
        val allEmployees = viewModel.employees.value ?: emptyList()
        val unassigned   = viewModel.getUnassignedCount()

        if (unassigned > 0) {
            binding.layoutUnassignedWarning.visibility = View.VISIBLE
            binding.tvUnassignedCount.text =
                "$unassigned employees not assigned to any shift this week"
        } else {
            binding.layoutUnassignedWarning.visibility = View.GONE
        }
    }

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
                putExtra("companyCode", companyCode)
                putExtra("companyName", companyName)
                putExtra("shiftCode", shift.shiftCode)
                putExtra("shiftName", shift.shiftName)
                putExtra("shiftStartTime", shift.startTime.toString())
                putExtra("shiftEndTime", shift.endTime.toString())
                putExtra("weekStartDate", viewModel.getSelectedWeekStart())
                putExtra("weekEndDate", viewModel.getSelectedWeekEnd())
            }
        )
    }
}