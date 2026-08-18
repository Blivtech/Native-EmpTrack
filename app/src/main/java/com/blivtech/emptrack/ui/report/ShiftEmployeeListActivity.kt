package com.blivtech.emptrack.ui.report

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.model.AttendanceEmployeeItem
import com.blivtech.emptrack.databinding.ActivityShiftEmployeeListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShiftEmployeeListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShiftEmployeeListBinding
    private val viewModel: ShiftEmployeeListViewModel by viewModels()

    private val btCode         by lazy { intent.getStringExtra("btCode") ?: "" }
    private val companyCode    by lazy { intent.getStringExtra("companyCode") ?: "" }
    private val companyName    by lazy { intent.getStringExtra("companyName") ?: "" }
    private val shiftCode      by lazy { intent.getStringExtra("shiftCode") ?: "" }
    private val shiftName      by lazy { intent.getStringExtra("shiftName") ?: "" }
    private val shiftTime      by lazy { intent.getStringExtra("shiftTime") ?: "" }
    private val shiftEmoji     by lazy { intent.getStringExtra("shiftEmoji") ?: "🕐" }
    private val attendanceDate by lazy { intent.getStringExtra("attendanceDate") ?: "" }
    private val type           by lazy { intent.getStringExtra("type") ?: "PRESENT" }
    private val count          by lazy { intent.getIntExtra("count", 0) }

    private var allEmployees   = listOf<AttendanceEmployeeItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShiftEmployeeListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupSearch()
        observeData()
    }

    // ─────────────────────────────────
    // ✅ Setup
    // ─────────────────────────────────
    private fun setupUI() {

        val topbarColor = when (type) {
            "PRESENT"    -> "#1B5E20"
            "ABSENT"     -> "#B71C1C"
            "HOLIDAY_WO" -> "#4A148C"
            else         -> "#1565C0"
        }
        binding.layoutTopbar.setBackgroundColor(
            Color.parseColor(topbarColor)
        )
        binding.layoutTopbar.setBackgroundColor(
            android.graphics.Color.parseColor(topbarColor)
        )

        binding.tvShiftTitle.text  = "$shiftEmoji $shiftName ($companyName)"
        binding.tvShiftSubtitle.text = "$attendanceDate"
        binding.tvShiftTime.text   = shiftTime



        binding.ivBack.setOnClickListener { finish() }

        binding.btnDownload.setOnClickListener {
            // TODO: PDF export
        }
    }

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

    // ✅ Replace old observe with new
    private fun observeData() {

        // ✅ Loading
        viewModel.loading.observe(this) { isLoading ->
//            binding.progressBar.visibility =
//                if (isLoading) View.VISIBLE else View.GONE
        }

        // ✅ Error
        viewModel.error.observe(this) { error ->
            error ?: return@observe
         //   Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
        }

        // ✅ Employee list
        viewModel.employees.observe(this) { list ->
            allEmployees = list
            renderList(list)
            binding.tvListLabel.text =
                "${list.size} employee${if (list.size != 1) "s" else ""}"
        }

        // ✅ Load from API
        viewModel.loadEmployees(
            btCode, companyCode,
            attendanceDate, shiftCode, type
        )
    }

    // ─────────────────────────────────
    // ✅ Filter by search
    // ─────────────────────────────────
    private fun filterEmployees(query: String) {
        binding.ivClearSearch.visibility =
            if (query.isNotEmpty()) View.VISIBLE else View.GONE

        val filtered = if (query.isEmpty()) allEmployees
        else allEmployees.filter {
            it.empName.contains(query, true) ||
            it.empCode.contains(query, true)
        }

        renderList(filtered)

        // ✅ Show search result count
        if (query.isNotEmpty()) {
            binding.tvListLabel.text =
                "${filtered.size} result${if (filtered.size != 1) "s" else ""} for \"$query\""
        } else {
            binding.tvListLabel.text =
                "${allEmployees.size} employee${if (allEmployees.size != 1) "s" else ""}"
        }
    }

    // ─────────────────────────────────
    // ✅ Render employee list
    // ─────────────────────────────────
    private fun renderList(employees: List<AttendanceEmployeeItem>) {
        binding.layoutEmployees.removeAllViews()

        if (employees.isEmpty()) {
            binding.layoutEmpty.visibility    = View.VISIBLE
            binding.layoutEmployees.visibility = View.GONE
            return
        }

        binding.layoutEmpty.visibility    = View.GONE
        binding.layoutEmployees.visibility = View.VISIBLE

        employees.forEach { emp ->
            val row = LayoutInflater.from(this).inflate(
                R.layout.item_report_employee_row,
                binding.layoutEmployees,
                false
            )

            // ✅ Avatar initials
            val initials = emp.empName.trim().split(" ").mapNotNull {
                it.firstOrNull()?.toString()
            }.take(2).joinToString("")
            row.findViewById<TextView>(R.id.tvAvatar).text = initials

            // ✅ Avatar bg color — deterministic from empCode
            val colors = listOf(
                "#E6F1FB" to "#0C447C",
                "#EAF3DE" to "#27500A",
                "#FAEEDA" to "#633806",
                "#EEEDFE" to "#3C3489",
                "#FCEBEB" to "#C62828",
                "#E1F5EE" to "#085041"
            )
            val colorPair = colors[emp.empCode.hashCode().and(0x7FFFFFFF) % colors.size]
         //   row.findViewById<android.view.View>(R.id.viewAvatarBg)
        //        .setBackgroundColor(android.graphics.Color.parseColor(colorPair.first))
            row.findViewById<TextView>(R.id.tvAvatar)
                .setTextColor(android.graphics.Color.parseColor(colorPair.second))

            // ✅ Name + code + dept
            row.findViewById<TextView>(R.id.tvEmpName).text   = emp.empName
            row.findViewById<TextView>(R.id.tvEmpCode).text   = emp.empCode
            row.findViewById<TextView>(R.id.tvEmpDept).text   = emp.desgName

            // ✅ Status label
            val tvStatus = row.findViewById<TextView>(R.id.tvStatus)
            tvStatus.text = emp.statusLabel
            when (emp.status) {
                "P"  -> {
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#27500A"))
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_green)
                }
                "A"  -> {
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#C62828"))
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_red)
                }
                "L"  -> {
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#633806"))
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_amber)
                }
                "H"  -> {
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#3C3489"))
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_purple)
                }
            }

            // ✅ Late minutes
            val tvLate = row.findViewById<TextView>(R.id.tvLateMinutes)
            if (emp.status == "L" && emp.lateMinutes > 0) {
                tvLate.visibility = View.VISIBLE
                tvLate.text = "+${emp.lateMinutes} min"
            } else {
                tvLate.visibility = View.GONE
            }

            // ✅ Red left border for absent
            if (emp.status == "A") {
                row.setBackgroundResource(R.drawable.bg_row_absent)
            }

            binding.layoutEmployees.addView(row)
        }
    }
}