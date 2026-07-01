package com.blivtech.emptrack.ui.report

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.model.DailyStatus
import com.blivtech.emptrack.data.model.EmployeeWeeklyDetail
import com.blivtech.emptrack.databinding.ActivityEmployeeWeeklyDetailBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class EmployeeWeeklyDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmployeeWeeklyDetailBinding
    private val viewModel: EmployeeWeeklyDetailViewModel by viewModels()

    private val btCode      by lazy { intent.getStringExtra("btCode")      ?: "" }
    private val companyCode by lazy { intent.getStringExtra("companyCode") ?: "" }
    private val shiftCode   by lazy { intent.getStringExtra("shiftCode")   ?: "" }
    private val shiftName   by lazy { intent.getStringExtra("shiftName")   ?: "" }
    private val shiftEmoji  by lazy { intent.getStringExtra("shiftEmoji")  ?: "🕐" }
    private val weekStart   by lazy { intent.getStringExtra("weekStart")   ?: "" }
    private val weekEnd     by lazy { intent.getStringExtra("weekEnd")     ?: "" }
    private val weekLabel   by lazy { intent.getStringExtra("weekLabel")   ?: "" }
    private val empCode     by lazy { intent.getStringExtra("empCode")     ?: "" }
    private val empName     by lazy { intent.getStringExtra("empName")     ?: "" }
    private val department  by lazy { intent.getStringExtra("department")  ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeWeeklyDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeData()
    }

    // ─────────────────────────────────
    // ✅ Setup
    // ─────────────────────────────────
    private fun setupUI() {
        binding.tvEmpTitle.text    = empName
        binding.tvEmpSubtitle.text = "$empCode · $shiftEmoji $shiftName · $weekLabel"
        binding.ivBack.setOnClickListener { finish() }
    }

    // ─────────────────────────────────
    // ✅ Observe
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

        viewModel.detail.observe(this) { detail ->
            detail ?: return@observe
            renderDetail(detail)
        }

        viewModel.loadDetail(
            btCode, companyCode,
            weekStart, weekEnd,
            shiftCode, empCode
        )
    }

    // ─────────────────────────────────
    // ✅ Render full detail
    // ─────────────────────────────────
    private fun renderDetail(detail: EmployeeWeeklyDetail) {

        // ✅ Count bar
        binding.tvCountPresent.text = detail.presentDays.toString()
        binding.tvCountAbsent.text  = detail.absentDays.toString()
        binding.tvCountLate.text    = detail.lateDays.toString()
        binding.tvCountHoliday.text = detail.holidayDays.toString()
        binding.tvCountWeekOff.text = detail.weekOffDays.toString()

        // ✅ Employee card
        val initials = detail.empName.trim().split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2).joinToString("")
        binding.tvEmpAvatar.text = initials
        binding.tvEmpName2.text  = detail.empName
        binding.tvEmpCode2.text  = "$empCode · ${detail.department}"
        binding.tvShiftTag.text  = "$shiftEmoji $shiftName"

        // ✅ Attendance percent + color
        binding.tvAttendancePercent.text = "${detail.attendancePercent}%"
        val pctColor = when {
            detail.attendancePercent >= 80 -> "#27500A"
            detail.attendancePercent >= 50 -> "#633806"
            else                           -> "#E24B4A"
        }
        binding.tvAttendancePercent.setTextColor(Color.parseColor(pctColor))

        // ✅ Warning
        if (detail.absentDays > detail.presentDays) {
            binding.layoutWarning.visibility = View.VISIBLE
            binding.tvWarningText.text =
                "${detail.absentDays} out of ${detail.totalDays} working days absent"
        } else {
            binding.layoutWarning.visibility = View.GONE
        }

        // ✅ No absence note
        binding.layoutNoAbsence.visibility =
            if (detail.absentDays == 0 && detail.lateDays == 0)
                View.VISIBLE else View.GONE

        // ✅ Build table
        buildTable(detail.dailyStatus)

        // ✅ Progress bars
        buildProgressBars(detail)
    }

    // ─────────────────────────────────
    // ✅ Build weekly table dynamically
    // ─────────────────────────────────
    private fun buildTable(days: List<DailyStatus>) {
        binding.layoutTableHeader.removeAllViews()
        binding.layoutTableRow.removeAllViews()
        binding.layoutTableSummary.removeAllViews()

        // ✅ Count per status for summary row
        var pCount = 0; var aCount = 0; var lCount = 0
        var hCount = 0; var wCount = 0

        days.forEach { day ->

            val isWeekOff = day.status == "W" || day.status == "WO"

            // ── Header cell ──
            val hdrCell = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
                orientation = LinearLayout.VERTICAL
                gravity     = Gravity.CENTER
                setPadding(4, 6, 4, 6)
                alpha = if (isWeekOff) 0.4f else 1f
            }
            val dayTv = TextView(this).apply {
                text      = day.dayName.take(3)
                textSize  =10f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(Color.parseColor("#9E9E9E"))
                gravity   = Gravity.CENTER
            }
            val dateTv = TextView(this).apply {
                text      = formatDayNum(day.date)
                textSize  = 10f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(Color.parseColor("#424242"))
                gravity   = Gravity.CENTER
            }
            hdrCell.addView(dayTv)
            hdrCell.addView(dateTv)
            binding.layoutTableHeader.addView(hdrCell)

            // ── Status badge cell ──
            val badgeCell = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
                orientation = LinearLayout.VERTICAL
                gravity     = Gravity.CENTER
                setPadding(3, 4, 3, 4)
                alpha = if (isWeekOff) 0.4f else 1f
            }

            val colors = statusColors(day.status)

            val badgeTv = TextView(this).apply {
                text      = when (day.status) {
                    "WO"  -> "W"
                    else  -> day.status
                }
                textSize  = 12f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(Color.parseColor(colors.second))
                setBackgroundColor(Color.parseColor(colors.first))
                gravity   = Gravity.CENTER
                width     = dpToPx(28)
                height    = dpToPx(28)
                setPadding(2, 2, 2, 2)
            }
            // ✅ Rounded corners via background drawable
            badgeTv.background = getRoundedBg(colors.first)
            badgeTv.setTextColor(Color.parseColor(colors.second))

            val lblTv = TextView(this).apply {
                text      = day.statusLabel.take(7)
                textSize  = 10f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(Color.parseColor(colors.second))
                gravity   = Gravity.CENTER
                setPadding(0, 3, 0, 0)
            }

            badgeCell.addView(badgeTv)
            badgeCell.addView(lblTv)
            binding.layoutTableRow.addView(badgeCell)

            // ✅ Count
            when (day.status) {
                "P"       -> pCount++
                "A"       -> aCount++
                "L"       -> lCount++
                "H"       -> hCount++
                "W", "WO" -> wCount++
            }
        }

        // ── Summary row ──
        val summaryData = listOf(
            Triple(pCount, "#27500A", "Present"),
            Triple(aCount, "#E24B4A", "Leave"),
            Triple(hCount, "#3C3489", "Holiday"),
            Triple(wCount, "#9E9E9E", "Week Off")
        )

        summaryData.forEach { (count, color, label) ->
            val cell = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
                orientation = LinearLayout.VERTICAL
                gravity     = Gravity.CENTER
                setPadding(2, 6, 2, 6)
            }
            val numTv = TextView(this).apply {
                text      = count.toString()
                textSize  = 13f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(Color.parseColor(color))
                gravity   = Gravity.CENTER
            }
            val lblTv = TextView(this).apply {
                text      = label
                textSize  = 12f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(Color.parseColor(color))
                gravity   = Gravity.CENTER
                setPadding(0, 2, 0, 0)
            }
            cell.addView(numTv)
            cell.addView(lblTv)
            binding.layoutTableSummary.addView(cell)
        }
    }

    // ─────────────────────────────────
    // ✅ Build progress bars
    // ─────────────────────────────────
    private fun buildProgressBars(detail: EmployeeWeeklyDetail) {
        val total = detail.totalDays.takeIf { it > 0 } ?: 1

        // ✅ Present
        binding.tvProgPresentLabel.text =
            "Present   ${detail.presentDays} / ${detail.totalDays} days"
        binding.progressPresent.progress =
            (detail.presentDays * 100 / total)

        // ✅ Late (show only if > 0)
        if (detail.lateDays > 0) {
            binding.tvProgLateLabel.visibility  = View.VISIBLE
            binding.progressLate.visibility     = View.VISIBLE
            binding.tvProgLateLabel.text =
                "Late   ${detail.lateDays} / ${detail.totalDays} days"
            binding.progressLate.progress =
                (detail.lateDays * 100 / total)
        } else {
            binding.tvProgLateLabel.visibility  = View.GONE
            binding.progressLate.visibility     = View.GONE
        }

        // ✅ Absent
        binding.tvProgAbsentLabel.text =
            "Leave   ${detail.absentDays} / ${detail.totalDays} days"
        binding.progressAbsent.progress =
            (detail.absentDays * 100 / total)

        // ✅ Holiday (show only if > 0)
        if (detail.holidayDays > 0) {
            binding.tvProgHolidayLabel.visibility = View.VISIBLE
            binding.progressHoliday.visibility    = View.VISIBLE
            binding.tvProgHolidayLabel.text =
                "Holiday   ${detail.holidayDays} / ${detail.totalDays} days"
            binding.progressHoliday.progress =
                (detail.holidayDays * 100 / total)
        } else {
            binding.tvProgHolidayLabel.visibility = View.GONE
            binding.progressHoliday.visibility    = View.GONE
        }
    }

    // ─────────────────────────────────
    // ✅ Helper — status bg + text color
    // ─────────────────────────────────
    private fun statusColors(status: String): Pair<String, String> {
        return when (status) {
            "P"       -> "#EAF3DE" to "#27500A"
            "A"       -> "#FCEBEB" to "#E24B4A"
            "L"       -> "#FAEEDA" to "#633806"
            "H"       -> "#EEEDFE" to "#3C3489"
            "W", "WO" -> "#F5F5F5" to "#9E9E9E"
            else      -> "#F5F5F5" to "#9E9E9E"
        }
    }

    // ✅ Rounded background for badge
    private fun getRoundedBg(hexColor: String): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape         = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius  = dpToPx(8).toFloat()
            setColor(Color.parseColor(hexColor))
        }
    }

    // ✅ dp → px
    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    // ✅ Format day number from date string
    private fun formatDayNum(date: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()
            cal.time = sdf.parse(date) ?: Date()
            cal.get(Calendar.DAY_OF_MONTH).toString()
        } catch (e: Exception) { date }
    }
}