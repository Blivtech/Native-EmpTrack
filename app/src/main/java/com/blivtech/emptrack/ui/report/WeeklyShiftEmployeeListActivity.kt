package com.blivtech.emptrack.ui.report

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.model.WeeklyShiftEmployee
import com.blivtech.emptrack.databinding.ActivityWeeklyShiftEmployeeListBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeeklyShiftEmployeeListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeeklyShiftEmployeeListBinding
    private val viewModel: WeeklyShiftEmployeeListViewModel by viewModels()

    private val btCode       by lazy { intent.getStringExtra("btCode")       ?: "" }
    private val companyCode  by lazy { intent.getStringExtra("companyCode")  ?: "" }
    private val companyName  by lazy { intent.getStringExtra("companyName")  ?: "" }
    private val shiftCode    by lazy { intent.getStringExtra("shiftCode")    ?: "" }
    private val shiftName    by lazy { intent.getStringExtra("shiftName")    ?: "" }
    private val shiftEmoji   by lazy { intent.getStringExtra("shiftEmoji")   ?: "🕐" }
    private val shiftTime    by lazy { intent.getStringExtra("shiftTime")    ?: "" }
    private val weekStart    by lazy { intent.getStringExtra("weekStart")    ?: "" }
    private val weekEnd      by lazy { intent.getStringExtra("weekEnd")      ?: "" }
    private val weekLabel    by lazy { intent.getStringExtra("weekLabel")    ?: "" }
    private val type         by lazy { intent.getStringExtra("type")         ?: "PRESENT" }
    private val presentCount by lazy { intent.getIntExtra("presentCount", 0) }
    private val absentCount  by lazy { intent.getIntExtra("absentCount", 0) }
    private val totalCount   by lazy { intent.getIntExtra("totalCount", 0) }

    private var allEmployees  = listOf<WeeklyShiftEmployee>()
    private var currentType   = "PRESENT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeeklyShiftEmployeeListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentType = type
        setupUI()
        setupTabs()
        setupSearch()
        observeData()
    }

    // ─────────────────────────────────
    // ✅ Setup
    // ─────────────────────────────────
    private fun setupUI() {
        binding.tvShiftTitle.text   = "$shiftEmoji $shiftName"
        binding.tvShiftSubtitle.text = "$weekLabel · $companyName"
        binding.tvShiftTime.text    = shiftTime

        // ✅ Topbar color
        updateTopbarColor()

        // ✅ Count bar
        binding.tvTotalPresent.text = presentCount.toString()
        binding.tvTotalAbsent.text  = absentCount.toString()
        binding.tvTotalCount.text   = totalCount.toString()

        binding.ivBack.setOnClickListener { finish() }
    }

    private fun updateTopbarColor() {
        val color = if (currentType == "PRESENT") "#1B5E20" else "#B71C1C"
        binding.layoutTopbar.setBackgroundColor(
            android.graphics.Color.parseColor(color)
        )
    }

    // ─────────────────────────────────
    // ✅ Tabs — Present / Absent
    // ─────────────────────────────────
    private fun setupTabs() {
        updateTabUI()

        binding.tabPresent.setOnClickListener {
            if (currentType != "PRESENT") {
                currentType = "PRESENT"
                updateTopbarColor()
                updateTabUI()
                loadEmployees()
            }
        }

        binding.tabAbsent.setOnClickListener {
            if (currentType != "ABSENT") {
                currentType = "ABSENT"
                updateTopbarColor()
                updateTabUI()
                loadEmployees()
            }
        }
    }

    private fun updateTabUI() {
        if (currentType == "PRESENT") {
            binding.tabPresent.alpha = 1f
            binding.tabAbsent.alpha  = 0.5f
            binding.tabPresentIndicator.visibility = View.VISIBLE
            binding.tabAbsentIndicator.visibility  = View.GONE
            binding.tvTabPresent.text = "✅ Present ($presentCount)"
            binding.tvTabAbsent.text  = "❌ Absent ($absentCount)"
        } else {
            binding.tabPresent.alpha = 0.5f
            binding.tabAbsent.alpha  = 1f
            binding.tabPresentIndicator.visibility = View.GONE
            binding.tabAbsentIndicator.visibility  = View.VISIBLE
        }
    }

    // ─────────────────────────────────
    // ✅ Search
    // ─────────────────────────────────
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterEmployees(s?.toString()?.trim() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        binding.ivClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }
    }

    private fun filterEmployees(query: String) {
        binding.ivClearSearch.visibility =
            if (query.isNotEmpty()) View.VISIBLE else View.GONE

        val filtered = if (query.isEmpty()) allEmployees
        else allEmployees.filter {
            it.empName.contains(query, true) ||
            it.empCode.contains(query, true)
        }

        renderList(filtered)
        binding.tvListLabel.text = if (query.isNotEmpty())
            "${filtered.size} result${if (filtered.size != 1) "s" else ""} for \"$query\""
        else "${allEmployees.size} employees"
    }

    // ─────────────────────────────────
    // ✅ Observe data
    // ─────────────────────────────────
    private fun observeData() {
        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error ?: return@observe
            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            viewModel.resetError()
        }

        viewModel.employees.observe(this) { list ->
            allEmployees = list
            renderList(list)
            binding.tvListLabel.text =
                "${list.size} employee${if (list.size != 1) "s" else ""}"
        }

        loadEmployees()
    }

    private fun loadEmployees() {
        binding.etSearch.text?.clear()
        viewModel.loadEmployees(
            btCode, companyCode,
            weekStart, weekEnd,
            shiftCode, currentType
        )
    }

    // ─────────────────────────────────
    // ✅ Render list
    // ─────────────────────────────────
    private fun renderList(employees: List<WeeklyShiftEmployee>) {
        binding.layoutEmployees.removeAllViews()

        if (employees.isEmpty()) {
            binding.layoutEmpty.visibility     = View.VISIBLE
            binding.layoutEmployees.visibility = View.GONE
            return
        }

        binding.layoutEmpty.visibility     = View.GONE
        binding.layoutEmployees.visibility = View.VISIBLE

        employees.forEach { emp ->
            val row = LayoutInflater.from(this).inflate(
                R.layout.item_weekly_employee_row,
                binding.layoutEmployees,
                false
            )

            // ✅ Avatar
            val initials = emp.empName.trim().split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2).joinToString("")
            row.findViewById<TextView>(R.id.tvAvatar).text = initials

            // ✅ Avatar color
            val colors = listOf(
                "#E6F1FB" to "#0C447C", "#EAF3DE" to "#27500A",
                "#FAEEDA" to "#633806", "#EEEDFE" to "#3C3489",
                "#FCEBEB" to "#C62828", "#E1F5EE" to "#085041"
            )
            val cp = colors[emp.empCode.hashCode().and(0x7FFFFFFF) % colors.size]
            row.findViewById<View>(R.id.viewAvatarBg)
                .setBackgroundColor(android.graphics.Color.parseColor(cp.first))
            row.findViewById<TextView>(R.id.tvAvatar)
                .setTextColor(android.graphics.Color.parseColor(cp.second))

            // ✅ Info
            row.findViewById<TextView>(R.id.tvEmpName).text   = emp.empName
            row.findViewById<TextView>(R.id.tvEmpCode).text   = emp.empCode
            row.findViewById<TextView>(R.id.tvEmpDept).text   = emp.department

            // ✅ Days pill
            val tvDays = row.findViewById<TextView>(R.id.tvDaysCount)
            val tvDaysLbl = row.findViewById<TextView>(R.id.tvDaysLabel)
            val pillBg = row.findViewById<View>(R.id.viewDaysPill)

            if (currentType == "PRESENT") {
                tvDays.text    = emp.presentDays.toString()
                tvDaysLbl.text = "days"
                tvDays.setTextColor(android.graphics.Color.parseColor("#0C447C"))
                pillBg.setBackgroundResource(R.drawable.bg_badge_blue)
                row.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"))
            } else {
                tvDays.text    = emp.absentDays.toString()
                tvDaysLbl.text = "absent"
                tvDays.setTextColor(android.graphics.Color.parseColor("#E24B4A"))
                pillBg.setBackgroundResource(R.drawable.bg_badge_red)
                row.setBackgroundResource(R.drawable.bg_row_absent)
            }

            // ✅ Tap → employee weekly detail
            row.setOnClickListener {
                openEmployeeDetail(emp)
            }

            binding.layoutEmployees.addView(row)
        }
    }

    // ─────────────────────────────────
    // ✅ Open employee detail
    // ─────────────────────────────────
    private fun openEmployeeDetail(emp: WeeklyShiftEmployee) {
        startActivity(
            Intent(this, EmployeeWeeklyDetailActivity::class.java).apply {
                putExtra("btCode",      btCode)
                putExtra("companyCode", companyCode)
                putExtra("shiftCode",   shiftCode)
                putExtra("shiftName",   shiftName)
                putExtra("shiftEmoji",  shiftEmoji)
                putExtra("weekStart",   weekStart)
                putExtra("weekEnd",     weekEnd)
                putExtra("weekLabel",   weekLabel)
                putExtra("empCode",     emp.empCode)
                putExtra("empName",     emp.empName)
                putExtra("department",  emp.department)
            }
        )
    }
}