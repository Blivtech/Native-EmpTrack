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
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.data.model.*
import com.blivtech.emptrack.databinding.ActivityMonthlyReportBinding
import com.blivtech.emptrack.utils.ShimmerHelper
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.blivtech.emptrack.utils.PreferenceManager
import androidx.lifecycle.lifecycleScope
import com.blivtech.emptrack.utils.MonthlyReportPdfBuilder
import com.blivtech.emptrack.utils.PdfFileOpener
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MonthlyReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMonthlyReportBinding
    private val viewModel: MonthlyReportViewModel by viewModels()

    @Inject lateinit var preferenceManager: PreferenceManager

    private var btCode      = ""
    private var companyCode = ""
    private var companyName = ""

    // ✅ State
    private var currentTab   = "OVERALL"    // OVERALL / SHIFT
    private var selectedShift: ShiftEntity? = null
    private var shifts = listOf<ShiftEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonthlyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            btCode      = preferenceManager.btCode.first()
            companyCode = preferenceManager.selectedCompanyCode.first()
            companyName = preferenceManager.selectedCompanyName.first()

            setupUI()
            setupTabs()
            observeData()
            loadData()
            setupDownloadBar()
        }
    }

    // ─────────────────────────────────
    // ✅ Setup UI
    // ─────────────────────────────────
    private fun setupUI() {
        binding.tvCompanyName.text = companyName
        binding.ivBack.setOnClickListener { finish() }

        // ✅ Month navigation
        binding.btnPrevMonth.setOnClickListener {
            viewModel.prevMonth()
            loadData()
        }
        binding.btnNextMonth.setOnClickListener {
            viewModel.nextMonth()
            loadData()
        }
    }

    // ─────────────────────────────────
    // ✅ Setup tabs
    // ─────────────────────────────────
    private fun setupTabs() {
        binding.tabOverall.setOnClickListener {
            if (currentTab != "OVERALL") {
                currentTab = "OVERALL"
                updateTabUI()
                binding.scrollShiftChips.visibility = View.GONE
                loadData()
            }
        }

        binding.tabShiftWise.setOnClickListener {
            if (currentTab != "SHIFT") {
                currentTab = "SHIFT"
                updateTabUI()
                binding.scrollShiftChips.visibility = View.VISIBLE
                loadData()
            }
        }
    }

    private fun updateTabUI() {
        binding.tabOverall.isSelected   = currentTab == "OVERALL"
        binding.tabShiftWise.isSelected = currentTab == "SHIFT"
    }

    // ─────────────────────────────────
    // ✅ Load shifts for chips
    // ─────────────────────────────────
    private fun buildShiftChips(shiftList: List<ShiftEntity>) {
        shifts = shiftList
        binding.layoutShiftChips.removeAllViews()

        shiftList.forEachIndexed { index, shift ->
            val chip = TextView(this).apply {
                text = "${shiftEmoji(shift.shiftName)} ${shift.shiftName}"
                textSize = 8f
                setTextColor(Color.parseColor("#9E9E9E"))
                setPadding(28, 14, 28, 14)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = 12 }
                setBackgroundResource(R.drawable.bg_badge_grey)
            }

            chip.setOnClickListener {
                selectedShift = shift
                // ✅ Update chip UI
                for (i in 0 until binding.layoutShiftChips.childCount) {
                    val v = binding.layoutShiftChips.getChildAt(i) as? TextView
                    v?.setBackgroundResource(R.drawable.bg_badge_grey)
                    v?.setTextColor(Color.parseColor("#9E9E9E"))
                }
                chip.setBackgroundResource(R.drawable.bg_card_working)
                chip.setTextColor(Color.parseColor("#27500A"))
                loadData()
            }

            // ✅ Auto-select first
            if (index == 0) {
                selectedShift = shift
                chip.setBackgroundResource(R.drawable.bg_card_working)
                chip.setTextColor(Color.parseColor("#27500A"))
            }

            binding.layoutShiftChips.addView(chip)
        }
    }

    // ─────────────────────────────────
    // ✅ Observe data
    // ─────────────────────────────────
    private fun observeData() {
        // ✅ Month label
        viewModel.monthLabel.observe(this) {
            binding.tvMonthLabel.text = it
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

        // ✅ Load shifts
        viewModel.getShifts(companyCode).observe(this) { shiftList ->
            buildShiftChips(shiftList)
        }
    }


    private fun loadData() {
        if (currentTab == "OVERALL") {
            viewModel.loadOverallReport(btCode, companyCode)
        } else {
            val shift = selectedShift ?: return
            viewModel.loadShiftReport(btCode, companyCode, shift.shiftCode)
        }
    }


    private fun updateSummaryStrip(
        present: Int, absent: Int,
        holiday: Int, weekOff: Int
    ) {
        binding.tvSumPresent.text = present.toString()
        binding.tvSumAbsent.text  = absent.toString()
        binding.tvSumHoliday.text = holiday.toString()
        binding.tvSumWeekOff.text = weekOff.toString()
    }


    private fun buildTable(employees: List<MonthlyEmployeeSummaryDto>) {
        binding.layoutTableRows.removeAllViews()

        if (employees.isEmpty()) {
            binding.layoutEmpty.visibility     = View.VISIBLE
            binding.layoutTableRows.visibility = View.GONE
            return
        }
        binding.layoutEmpty.visibility     = View.GONE
        binding.layoutTableRows.visibility = View.VISIBLE

        employees.forEach { emp ->
            val row = LayoutInflater.from(this).inflate(
                R.layout.item_monthly_report_row, binding.layoutTableRows, false
            )
            row.findViewById<TextView>(R.id.tvEmpName).text = emp.empName
            row.findViewById<TextView>(R.id.tvEmpCode).text = emp.empCode

            val tvP  = row.findViewById<TextView>(R.id.tvP)
            val tvL  = row.findViewById<TextView>(R.id.tvL)
            val tvH  = row.findViewById<TextView>(R.id.tvH)
            val tvWO = row.findViewById<TextView>(R.id.tvWO)

            tvP.text  = emp.presentDays.toString()
            tvL.text  = emp.absentDays.toString()
            tvH.text  = emp.holidayDays.toString()
            tvWO.text = emp.weekOffDays.toString()
            row.findViewById<TextView>(R.id.tvTotal).text = emp.totalDays.toString()

            tvP.setOnClickListener  { openEmployeeDetail(emp, "PRESENT") }
            tvL.setOnClickListener  { openEmployeeDetail(emp, "ABSENT") }
            tvH.setOnClickListener  { openEmployeeDetail(emp, "HOLIDAY") }
            tvWO.setOnClickListener { openEmployeeDetail(emp, "WEEKOFF") }

            binding.layoutTableRows.addView(row)
        }
    }

    // ─────────────────────────────────
    // ✅ Open employee date detail
    // ─────────────────────────────────
    private fun openEmployeeDetail(
        emp: MonthlyEmployeeSummaryDto,
        type: String    // PRESENT / ABSENT / HOLIDAY / WEEKOFF
    ) {
        startActivity(
            Intent(
                this,
                MonthlyEmployeeDetailActivity::class.java
            ).apply {
                putExtra("btCode",      btCode)
                putExtra("companyCode", companyCode)
                putExtra("month",       viewModel.currentMonth.value)
                putExtra("monthLabel",  viewModel.monthLabel.value)
                putExtra("shiftCode",   emp.shiftCode)
                putExtra("shiftName",   emp.shiftName)
                putExtra("empCode",     emp.empCode)
                putExtra("empName",     emp.empName)
                putExtra("deptName",    emp.deptName)
                putExtra("desgName",    emp.desgName)
                putExtra("type",        type)   // ✅ which tab to show first
            }
        )
    }

    // ✅ Emoji helper
    private fun shiftEmoji(name: String) = when {
        name.contains("morning", true) ||
        name.contains("day", true)     -> "☀️"
        name.contains("evening", true) -> "🌆"
        name.contains("night", true)   -> "🌙"
        else                           -> "🕐"
    }

    // ✅ Add to MonthlyReportActivity.kt

    private fun setupDownloadBar() {
        binding.imgDownload.setOnClickListener {
            generateAndOpenPdf()
        }
    }

    private fun generateAndOpenPdf() {
        val monthLabel = viewModel.monthLabel.value ?: return

        val file = when (currentTab) {
            "OVERALL" -> {
                val report = viewModel.overallReport.value ?: run {
                    Snackbar.make(binding.root, "No data to export", Snackbar.LENGTH_SHORT).show()
                    return
                }
                MonthlyReportPdfBuilder.buildOverallPdf(
                    context     = this,
                    companyName = companyName,
                    monthLabel  = monthLabel,
                    workingDays = report.workingDays,
                    report      = report
                )
            }
            "SHIFT" -> {
                // ✅ For shift wise PDF you need ALL shift reports loaded, not just the selected chip.
                // Simplest: loop through `shifts` and fetch each report synchronously before building.
                // (See note below for a batched-load helper if you want this automated.)
                val report = viewModel.shiftReport.value ?: run {
                    Snackbar.make(binding.root, "No data to export", Snackbar.LENGTH_SHORT).show()
                    return
                }
                MonthlyReportPdfBuilder.buildShiftWisePdf(
                    context      = this,
                    companyName  = companyName,
                    monthLabel   = monthLabel,
                    workingDays  = report.workingDays,
                    shiftReports = listOf(report)   // currently selected shift only — see note
                )
            }
            else -> return
        }

        PdfFileOpener.openPdf(this, file)
    }
}