package com.blivtech.emptrack.ui.report

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.model.MonthlyEmployeeDetail
import com.blivtech.emptrack.databinding.ActivityMonthlyEmployeeDetailBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

// Small holder for a rendered date chip.
data class DayItem(val dateLabel: String, val weekday: String)

@AndroidEntryPoint
class MonthlyEmployeeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMonthlyEmployeeDetailBinding
    private val viewModel: MonthlyEmployeeDetailViewModel by viewModels()

    private val btCode      by lazy { intent.getStringExtra("btCode")      ?: "" }
    private val companyCode by lazy { intent.getStringExtra("companyCode") ?: "" }
    private val month       by lazy { intent.getStringExtra("month")       ?: "" }
    private val monthLabel  by lazy { intent.getStringExtra("monthLabel")  ?: "" }
    private val shiftCode   by lazy { intent.getStringExtra("shiftCode")   ?: "" }
    private val shiftName   by lazy { intent.getStringExtra("shiftName")   ?: "" }
    private val empCode     by lazy { intent.getStringExtra("empCode")     ?: "" }
    private val empName     by lazy { intent.getStringExtra("empName")     ?: "" }
    private val deptName    by lazy { intent.getStringExtra("deptName")    ?: "" }
    private val desgName    by lazy { intent.getStringExtra("desgName")    ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonthlyEmployeeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeData()
    }

    // ─────────────────────────────────
    private fun setupUI() {
        binding.tvEmpTitle.text    = empName
        binding.tvEmpSubtitle.text = "$empCode · $shiftName · $monthLabel"
        binding.ivBack.setOnClickListener { finish() }
        binding.tvEmpName.text = empName
        binding.tvEmpMeta.text = listOf(empCode, deptName, desgName).filter { it.isNotBlank() }.joinToString(" · ")
        binding.tvAvatar.text  = empName.trim().take(1).uppercase(Locale.getDefault())
        binding.tvShift.text   = if (shiftName.isNotBlank()) "🕐 $shiftName" else ""
        if (shiftName.isBlank()) binding.tvShift.visibility = View.GONE
    }

    // ─────────────────────────────────
    private fun observeData() {
        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(this) { error ->
            error ?: return@observe
            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            viewModel.resetError()
        }
        viewModel.detail.observe(this) { detail ->
            detail ?: return@observe
            renderDetail(detail)
        }
        viewModel.loadDetail(btCode, companyCode, month, shiftCode, empCode)
    }

    // ─────────────────────────────────
    // Single view: build 3 sections from the detail's date lists.
    // NOTE: adjust the four list names below to match MonthlyEmployeeDetail.
    // (Assumed: presentDates / absentDates / holidayDates / weekOffDates : List<String> "yyyy-MM-dd")
    // ─────────────────────────────────
    private fun renderDetail(detail: MonthlyEmployeeDetail) {
        val presentRaw = detail.presentDates
        val absentRaw  = detail.absentDates
        val holidayRaw = detail.holidayDates
        val weekOffRaw = detail.weekOffDates

        // tiles
        binding.tvSumPresent.text = presentRaw.size.toString()
        binding.tvSumLeave.text   = absentRaw.size.toString()
        binding.tvSumHoliday.text = holidayRaw.size.toString()
        binding.tvSumWeekOff.text = weekOffRaw.size.toString()

        // attendance %
        val total = presentRaw.size + absentRaw.size + holidayRaw.size + weekOffRaw.size
        val pct = if (total > 0) presentRaw.size * 100 / total else 0
        binding.tvAttendance.text = "$pct%"

        // sections
        val present = presentRaw.map { DayItem(formatDate(it), getDayName(it)) }
        val absent  = absentRaw.map  { DayItem(formatDate(it), getDayName(it)) }
        val hwo     = (holidayRaw + weekOffRaw).sorted().map { DayItem(formatDate(it), getDayName(it)) }

        binding.tvPresentTitle.text = "Present Dates (${present.size})"
        binding.tvAbsentTitle.text  = "Absent Dates (${absent.size})"
        binding.tvHwoTitle.text     = "Holiday + Week Off (${hwo.size})"

        renderChips(binding.layoutPresentChips, binding.tvPresentEmpty, present, R.drawable.bg_date_present, "#15803D")
        renderChips(binding.layoutAbsentChips,  binding.tvAbsentEmpty,  absent,  R.drawable.bg_date_absent,  "#B91C1C")
        renderChips(binding.layoutHwoChips,     binding.tvHwoEmpty,     hwo,     R.drawable.bg_date_hwo,     "#FFFFFF")
    }

    // ─────────────────────────────────
    private fun renderChips(
        container: LinearLayout,
        emptyView: View,
        dates: List<DayItem>,
        bgRes: Int,
        dateColor: String
    ) {
        container.removeAllViews()
        if (dates.isEmpty()) {
            container.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            return
        }
        container.visibility = View.VISIBLE
        emptyView.visibility = View.GONE

        val perRow = 5
        dates.chunked(perRow).forEach { rowItems ->
            val row = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(9) }
                orientation = LinearLayout.HORIZONTAL
            }
            rowItems.forEach { item ->
                val chip = layoutInflater.inflate(R.layout.item_date_chip, row, false)
                chip.setBackgroundResource(bgRes)
                chip.findViewById<TextView>(R.id.tvDate).apply {
                    text = item.dateLabel; setTextColor(Color.parseColor(dateColor))
                }
                chip.findViewById<TextView>(R.id.tvWeekday).apply {
                    text = item.weekday
                    setTextColor(Color.parseColor(if (dateColor == "#FFFFFF") "#EDE9FE" else "#64748B"))
                }
                row.addView(chip)
            }
            repeat(perRow - rowItems.size) {
                row.addView(View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, 1, 1f).apply {
                        marginStart = dp(4); marginEnd = dp(4)
                    }
                })
            }
            container.addView(row)
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun formatDate(date: String): String = try {
        val inp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val out = SimpleDateFormat("d MMM", Locale.getDefault())
        out.format(inp.parse(date) ?: Date())
    } catch (e: Exception) { date }

    private fun getDayName(date: String): String = try {
        val inp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val out = SimpleDateFormat("EEE", Locale.getDefault())
        out.format(inp.parse(date) ?: Date())
    } catch (e: Exception) { "" }
}