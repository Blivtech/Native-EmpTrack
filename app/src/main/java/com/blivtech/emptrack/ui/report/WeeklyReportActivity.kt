package com.blivtech.emptrack.ui.report

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.data.model.WeeklyEmployeeSummaryDto
import com.blivtech.emptrack.databinding.ActivityWeeklyReportBinding
import com.blivtech.emptrack.ui.report.adapter.WeeklyReportAdapter
import com.blivtech.emptrack.utils.PreferenceManager
import com.blivtech.emptrack.utils.Resource
import com.blivtech.emptrack.utils.ShimmerHelper
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WeeklyReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeeklyReportBinding
    private val viewModel: WeeklyReportViewModel by viewModels()
    private val weeklyAdapter by lazy {
        WeeklyReportAdapter { emp, type -> openEmployeeDetail(emp, type) }
    }

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var btCode      = ""
    private var companyCode = ""
    private var companyName = ""

    // ✅ State
    private var currentTab    = "OVERALL"
    private var selectedShift: ShiftEntity? = null
    private var shifts        = listOf<ShiftEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeeklyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            btCode      = preferenceManager.btCode.first()
            companyCode = preferenceManager.selectedCompanyCode.first()
            companyName = preferenceManager.selectedCompanyName.first()

            setupUI()
            setupTabs()
            observeData()
        }
    }

    // ─────────────────────────────────
    // ✅ Setup UI
    // ─────────────────────────────────
    private fun setupUI() {
        binding.tvCompanyName.text = companyName
        binding.ivBack.setOnClickListener { finish() }

        binding.btnPrevWeek.setOnClickListener {
            viewModel.prevWeek()
            loadData()
        }
        binding.btnNextWeek.setOnClickListener {
            viewModel.nextWeek()
            loadData()
        }
        binding.rvWeeklyRows.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.rvWeeklyRows.adapter = weeklyAdapter
    }

    // ─────────────────────────────────
    // ✅ Setup tabs
    // ─────────────────────────────────
    private fun setupTabs() {
        binding.tabOverall.setOnClickListener {
            if (currentTab != "OVERALL") {
                currentTab = "OVERALL"
                updateTabUI()
               // binding.scrollShiftChips.visibility = View.GONE
                loadData()
            }
        }

        binding.tabShiftWise.setOnClickListener {
            if (currentTab != "SHIFT") {
                currentTab = "SHIFT"
                updateTabUI()
            //    binding.scrollShiftChips.visibility = View.VISIBLE
                loadData()
            }
        }

        updateTabUI()
    }

    private fun updateTabUI() {
        if (currentTab == "OVERALL") {
            binding.tabOverall.alpha   = 1f
            binding.tabShiftWise.alpha = 0.5f
            binding.tabShiftWise.setTextColor(ContextCompat.getColor(this, R.color.bg_ss))
            binding.tabOverall.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            binding.tabOverall.alpha   = 0.5f
            binding.tabShiftWise.alpha = 1f
            binding.tabShiftWise.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.tabOverall.setTextColor(ContextCompat.getColor(this, R.color.bg_ss))
        }
    }

    // ─────────────────────────────────
    // ✅ Build shift chips
    // ─────────────────────────────────
    private fun buildShiftChips(shiftList: List<ShiftEntity>) {
        shifts = shiftList
        binding.layoutShiftChips.removeAllViews()

        shiftList.forEachIndexed { index, shift ->
            val chip = TextView(this).apply {
                text = "${viewModel.shiftEmoji(shift.shiftName)} ${shift.shiftName}"
                textSize = 12f
                setTextColor(Color.parseColor("#9E9E9E"))
                setPadding(28, 14, 28, 14)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = 10 }
                setBackgroundResource(R.drawable.bg_badge_grey)
            }

            chip.setOnClickListener {
                selectedShift = shift
                // ✅ Reset all chips
                for (i in 0 until binding.layoutShiftChips.childCount) {
                    val v = binding.layoutShiftChips.getChildAt(i) as? TextView
                    v?.setBackgroundResource(R.drawable.bg_badge_grey)
                    v?.setTextColor(Color.parseColor("#9E9E9E"))
                }
                // ✅ Highlight selected
                chip.setBackgroundResource(R.drawable.bg_card_working)
                chip.setTextColor(Color.parseColor("#27500A"))
                loadData()
            }

            // ✅ Auto select first
            if (index == 0) {
                selectedShift = shift
                chip.setBackgroundResource(R.drawable.bg_card_working)
                chip.setTextColor(Color.parseColor("#27500A"))
            }

            binding.layoutShiftChips.addView(chip)
        }
    }

    private fun observeData() {

        viewModel.weekLabel.observe(this) {
            binding.tvWeekLabel.text = it
        }
        viewModel.weekSubLabel.observe(this) {
            binding.tvWeekSubLabel.text = it
        }

        // ✅ Loading
        viewModel.loading.observe(this) { isLoading ->
            if (isLoading) {
                ShimmerHelper.show(
                    binding.shimmerLayout,
                    binding.layoutTable,
                    binding.layoutSummaryStrip
                )
            } else {
                ShimmerHelper.hide(
                    binding.shimmerLayout,
                    binding.layoutTable,
                    binding.layoutSummaryStrip
                )
            }
        }

        // ✅ Error
        viewModel.error.observe(this) { error ->
            error ?: return@observe
            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            viewModel.resetError()
        }

        // ✅ Overall report
        viewModel.overallReport.observe(this) { report ->
            report ?: return@observe
            updateSummaryStrip(
                report.totalPresent,
                report.totalAbsent,
                report.totalHoliday,
                report.totalWeekOff
            )
            buildTable(report.employees)
        }

        // ✅ Shift report
        viewModel.shiftReport.observe(this) { report ->
            report ?: return@observe
            updateSummaryStrip(
                report.totalPresent,
                report.totalAbsent,
                report.totalHoliday,
                report.totalWeekOff
            )
            buildTable(report.employees)
        }

        // ✅ Shifts from Room
        viewModel.getShifts(companyCode).observe(this) { shiftList ->
            buildShiftChips(shiftList)
        }

        loadData()
    }

    // ─────────────────────────────────
    // ✅ Load data based on tab
    // ─────────────────────────────────
    private fun loadData() {
        if (currentTab == "OVERALL") {
            viewModel.loadOverallReport(btCode, companyCode)
        } else {
            val shift = selectedShift ?: return
            viewModel.loadShiftReport(
                btCode, companyCode, shift.shiftCode
            )
        }
    }

    // ─────────────────────────────────
    // ✅ Update summary strip
    // ─────────────────────────────────
    private fun updateSummaryStrip(
        present: Int, absent: Int,
        holiday: Int, weekOff: Int
    ) {
        binding.tvPresent.text = present.toString()
        binding.tvLeave.text  = absent.toString()
        binding.tvHoliday.text = holiday.toString()
        binding.tvWeekOff.text = weekOff.toString()
    }


    private fun buildTable(employees: List<WeeklyEmployeeSummaryDto>) {
        if (employees.isEmpty()) {
            binding.layoutEmpty.visibility     = View.VISIBLE
            binding.rvWeeklyRows.visibility    = View.GONE
        } else {
            binding.layoutEmpty.visibility     = View.GONE
            binding.rvWeeklyRows.visibility    = View.VISIBLE
            weeklyAdapter.submitList(employees)
        }
        binding.layoutTable.visibility = View.VISIBLE
    }

    // ─────────────────────────────────
    // ✅ Open employee weekly detail
    // ─────────────────────────────────
    private fun openEmployeeDetail(
        emp: WeeklyEmployeeSummaryDto,
        type: String
    ) {
        startActivity(
            Intent(
                this,
                EmployeeWeeklyDetailActivity::class.java
            ).apply {
                putExtra("btCode",      btCode)
                putExtra("companyCode", companyCode)
                putExtra("shiftCode",   emp.shiftCode)
                putExtra("shiftName",   emp.shiftName)
                putExtra("shiftEmoji",  viewModel.shiftEmoji(emp.shiftName))
                putExtra("weekStart",   viewModel.weekStart.value)
                putExtra("weekEnd",     viewModel.weekEnd.value)
                putExtra("weekLabel",   viewModel.weekLabel.value)
                putExtra("empCode",     emp.empCode)
                putExtra("empName",     emp.empName)
                putExtra("department",  emp.deptName)
                putExtra("type",        type)
            }
        )
    }
}