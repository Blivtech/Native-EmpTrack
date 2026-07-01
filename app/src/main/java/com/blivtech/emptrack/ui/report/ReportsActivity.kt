package com.blivtech.emptrack.ui.report

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.model.ReportItem
import com.blivtech.emptrack.databinding.ActivityReportsBinding
import com.blivtech.emptrack.ui.report.adapter.ReportAdapter
import com.blivtech.emptrack.utils.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private lateinit var reportAdapter: ReportAdapter

    private var companyName = ""
    private var companyCode = ""
    private var btCode      = ""

    private val monthFmt = SimpleDateFormat(
        "MMM yyyy", Locale.getDefault()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            btCode      = preferenceManager.btCode.first()
            companyCode = preferenceManager.selectedCompanyCode.first()
            companyName = preferenceManager.selectedCompanyName.first()

            setupUI()
            setupRecyclerView()
            loadReports()
        }
    }

    // ─────────────────────────────────
    // ✅ Setup UI
    // ─────────────────────────────────
    private fun setupUI() {
        binding.tvCompanyName.text = companyName
        binding.tvMonth.text       = monthFmt.format(Date())
        binding.ivBack.setOnClickListener { finish() }
    }

    // ─────────────────────────────────
    // ✅ Setup RecyclerView
    // ─────────────────────────────────
    private fun setupRecyclerView() {
        reportAdapter = ReportAdapter { item ->
            openReport(item)
        }

        binding.rvReports.apply {
            adapter       = reportAdapter
            layoutManager = LinearLayoutManager(
                this@ReportsActivity
            )
            isNestedScrollingEnabled = false
        }
    }

    // ─────────────────────────────────
    // ✅ Build report list
    // ─────────────────────────────────
    private fun loadReports() {
        val reports = listOf(

            // ── Attendance Reports ──────────────
            ReportItem(
                id              = "DAILY",
                name            = "Daily Report",
                subtitle        = "Shift-wise attendance by date",
                category        = "ATTENDANCE",
                tag             = "Daily",
                iconRes         = R.drawable.ic_nav_reports,
                iconBgColor     = "#E6F1FB",
                iconTintColor   = "#0C447C",
                destination     = DailyReportActivity::class.java
            ),

            ReportItem(
                id              = "WEEKLY",
                name            = "Weekly Report",
                subtitle        = "Shift-wise summary by week",
                category        = "ATTENDANCE",
                tag             = "Weekly",
                iconRes         = R.drawable.ic_nav_reports,
                iconBgColor     = "#EAF3DE",
                iconTintColor   = "#27500A",
                destination     = WeeklyReportActivity::class.java
            ),

            ReportItem(
                id              = "MONTHLY",
                name            = "Monthly Report",
                subtitle        = "Monthly attendance summary",
                category        = "ATTENDANCE",
                tag             = "Monthly",
                iconRes         = R.drawable.ic_nav_reports,
                iconBgColor     = "#EEEDFE",
                iconTintColor   = "#3C3489",
                destination     = MonthlyReportActivity::class.java
            ),
//
//            // ── Wage Reports ────────────────────
//            ReportItem(
//                id              = "ADVANCE",
//                name            = "Advance Report",
//                subtitle        = "Employee advance payments",
//                category        = "WAGES",
//                tag             = "Monthly",
//                iconRes         = R.drawable.ic_cash,
//                iconBgColor     = "#FAEEDA",
//                iconTintColor   = "#633806",
//                destination     = AdvanceReportActivity::class.java
//            ),
//
//            ReportItem(
//                id              = "OVERTIME",
//                name            = "Overtime Report",
//                subtitle        = "Employee OT hours & amount",
//                category        = "WAGES",
//                tag             = "Monthly",
//                iconRes         = R.drawable.ic_clock_plus,
//                iconBgColor     = "#FCEBEB",
//                iconTintColor   = "#C62828",
//                destination     = OvertimeReportActivity::class.java
//            ),
//
//            ReportItem(
//                id              = "BONUS",
//                name            = "Bonus Report",
//                subtitle        = "Employee bonus breakdown",
//                category        = "WAGES",
//                tag             = "Monthly",
//                iconRes         = R.drawable.ic_gift,
//                iconBgColor     = "#E1F5EE",
//                iconTintColor   = "#085041",
//                destination     = BonusReportActivity::class.java
//            ),
//
//            // ── Work Reports ────────────────────
//            ReportItem(
//                id              = "WORK_PROGRESS",
//                name            = "Work Progress Report",
//                subtitle        = "Product wise work summary",
//                category        = "WORK",
//                tag             = "Monthly",
//                iconRes         = R.drawable.ic_chart_bar,
//                iconBgColor     = "#FFF3E0",
//                iconTintColor   = "#E65100",
//                destination     = WorkProgressReportActivity::class.java
//            )
        )

        reportAdapter.submitList(reports)
    }

    // ─────────────────────────────────
    // ✅ Open report
    // ─────────────────────────────────
    private fun openReport(item: ReportItem) {
        startActivity(
            Intent(this, item.destination).apply {
                putExtra("btCode",      btCode)
                putExtra("companyCode", companyCode)
                putExtra("companyName", companyName)
            }
        )
    }
}