package com.blivtech.emptrack.ui.report

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.model.AdvanceEmployeeDto
import com.blivtech.emptrack.databinding.ActivityAdvanceReportBinding
import com.blivtech.emptrack.utils.PreferenceManager
import com.blivtech.emptrack.utils.Resource
import com.blivtech.emptrack.utils.ShimmerHelper
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AdvanceReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdvanceReportBinding
    private val viewModel: AdvanceReportViewModel by viewModels()

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var btCode      = ""
    private var companyCode = ""
    private var companyName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdvanceReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            btCode      = preferenceManager.btCode.first()
            companyCode = preferenceManager.selectedCompanyCode.first()
            companyName = preferenceManager.selectedCompanyName.first()

            setupUI()
            observeData()
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
    // ✅ Observe data
    // ─────────────────────────────────
    private fun observeData() {

        // ✅ Month label
        viewModel.monthLabel.observe(this) {
            binding.tvMonthLabel.text = it
        }
        viewModel.monthSubLabel.observe(this) {
            binding.tvMonthSubLabel.text = "${it} advance entries"
        }

        // ✅ Loading
        viewModel.loading.observe(this) { isLoading ->
            if (isLoading) {
                ShimmerHelper.show(
                    binding.shimmerLayout,
                    binding.layoutEmployees,
                    binding.layoutSummaryStrip
                )
            } else {
                ShimmerHelper.hide(
                    binding.shimmerLayout,
                    binding.layoutEmployees,
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

        // ✅ Report
        viewModel.report.observe(this) { report ->
            report ?: return@observe

            // ✅ Summary strip
            binding.tvTotalEmployees.text = report.totalEmployees.toString()
            binding.tvTotalAmount.text    =
                viewModel.formatAmount(report.totalAmount)
            binding.tvMonthSubLabel.text  =
                "${report.entries.size} advance entries"

            // ✅ Render employees
            renderEmployees(report.entries)
        }

        loadData()
    }

    // ─────────────────────────────────
    // ✅ Load data
    // ─────────────────────────────────
    private fun loadData() {
        viewModel.loadReport(btCode, companyCode)
    }

    // ─────────────────────────────────
    // ✅ Render employee cards
    // ─────────────────────────────────
    private fun renderEmployees(entries: List<AdvanceEmployeeDto>) {
        binding.layoutEmployees.removeAllViews()

        if (entries.isEmpty()) {
            binding.layoutEmpty.visibility     = View.VISIBLE
            binding.layoutEmployees.visibility = View.GONE
            return
        }

        binding.layoutEmpty.visibility     = View.GONE
        binding.layoutEmployees.visibility = View.VISIBLE

        entries.forEach { emp ->
            val row = LayoutInflater.from(this).inflate(
                R.layout.item_advance_employee_card,
                binding.layoutEmployees,
                false
            )

            // ✅ Avatar initials
            val initials = emp.empName.trim().split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2).joinToString("")
            row.findViewById<TextView>(R.id.tvAvatar).text = initials

            // ✅ Avatar color
            val colors = listOf(
                "#E6F1FB" to "#0C447C",
                "#EAF3DE" to "#27500A",
                "#FAEEDA" to "#633806",
                "#EEEDFE" to "#3C3489",
                "#FCEBEB" to "#C62828",
                "#E1F5EE" to "#085041"
            )
            val cp = colors[
                emp.empCode.hashCode()
                    .and(0x7FFFFFFF) % colors.size
            ]
            row.findViewById<View>(R.id.viewAvatarBg)
                .setBackgroundColor(Color.parseColor(cp.first))
            row.findViewById<TextView>(R.id.tvAvatar)
                .setTextColor(Color.parseColor(cp.second))

            // ✅ Name + info
            row.findViewById<TextView>(R.id.tvEmpName).text  = emp.empName
            row.findViewById<TextView>(R.id.tvEmpCode).text  = emp.empCode
            row.findViewById<TextView>(R.id.tvDeptName).text = emp.deptName

            // ✅ Amount
            row.findViewById<TextView>(R.id.tvAmount).text =
                viewModel.formatAmount(emp.totalAmount)

            // ✅ Date
            row.findViewById<TextView>(R.id.tvDate).text =
                formatDisplayDate(emp.lastDate)

            // ✅ Tap → employee detail
            row.setOnClickListener {
                openEmployeeDetail(emp)
            }

            binding.layoutEmployees.addView(row)
        }

        // ✅ Total row
        val totalRow = LayoutInflater.from(this).inflate(
            R.layout.item_advance_total_row,
            binding.layoutEmployees,
            false
        )
        val totalAmount = entries.sumOf { it.totalAmount }
        totalRow.findViewById<TextView>(R.id.tvTotalLabel).text =
            "Total — ${viewModel.monthLabel.value}"
        totalRow.findViewById<TextView>(R.id.tvTotalAmount).text =
            viewModel.formatAmount(totalAmount)
        binding.layoutEmployees.addView(totalRow)
    }

    // ─────────────────────────────────
    // ✅ Open employee detail
    // ─────────────────────────────────
    private fun openEmployeeDetail(emp: AdvanceEmployeeDto) {
        startActivity(
            Intent(
                this,
                AdvanceEmployeeDetailActivity::class.java
            ).apply {
                putExtra("btCode",      btCode)
                putExtra("companyCode", companyCode)
                putExtra("month",       viewModel.currentMonth.value)
                putExtra("monthLabel",  viewModel.monthLabel.value)
                putExtra("empCode",     emp.empCode)
                putExtra("empName",     emp.empName)
                putExtra("deptName",    emp.deptName)
                putExtra("desgName",    emp.desgName)
            }
        )
    }

    // ─────────────────────────────────
    // ✅ Format date
    // ─────────────────────────────────
    private fun formatDisplayDate(date: String): String {
        return try {
            val inp = java.text.SimpleDateFormat(
                "yyyy-MM-dd", java.util.Locale.getDefault()
            )
            val out = java.text.SimpleDateFormat(
                "dd MMM yyyy", java.util.Locale.getDefault()
            )
            out.format(inp.parse(date) ?: java.util.Date())
        } catch (e: Exception) { date }
    }
}