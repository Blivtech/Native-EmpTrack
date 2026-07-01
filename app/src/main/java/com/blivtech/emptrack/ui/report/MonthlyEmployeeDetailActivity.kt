package com.blivtech.emptrack.ui.report

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.model.MonthlyEmployeeDetail
import com.blivtech.emptrack.databinding.ActivityMonthlyEmployeeDetailBinding
import com.blivtech.emptrack.utils.ShimmerHelper
import com.google.android.material.snackbar.Snackbar
import com.google.android.flexbox.FlexboxLayout
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

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
    private val initialType by lazy { intent.getStringExtra("type")        ?: "PRESENT" }

    private var currentType = "PRESENT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonthlyEmployeeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentType = initialType
        setupUI()
        setupTabs()
        observeData()
    }

    // ─────────────────────────────────
    // ✅ Setup
    // ─────────────────────────────────
    private fun setupUI() {
        binding.tvEmpTitle.text    = empName
        binding.tvEmpSubtitle.text = "$empCode · $shiftName · $monthLabel"
        binding.ivBack.setOnClickListener { finish() }
    }

    // ─────────────────────────────────
    // ✅ Tabs
    // ─────────────────────────────────
    private fun setupTabs() {
        updateTabUI()

        binding.tabPresent.setOnClickListener {
            currentType = "PRESENT"
            updateTabUI()
            renderDates(viewModel.detail.value)
        }
        binding.tabAbsent.setOnClickListener {
            currentType = "ABSENT"
            updateTabUI()
            renderDates(viewModel.detail.value)
        }
        binding.tabHolidayWO.setOnClickListener {
            currentType = "HOLIDAY_WO"
            updateTabUI()
            renderDates(viewModel.detail.value)
        }
    }

    private fun updateTabUI() {
        // ✅ Reset all
        listOf(
            binding.tabPresent,
            binding.tabAbsent,
            binding.tabHolidayWO
        ).forEach {
            it.alpha = 0.5f
        }
        // ✅ Highlight selected
        when (currentType) {
            "PRESENT"    -> binding.tabPresent.alpha   = 1f
            "ABSENT"     -> binding.tabAbsent.alpha    = 1f
            "HOLIDAY_WO" -> binding.tabHolidayWO.alpha = 1f
        }
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
            btCode, companyCode, month, shiftCode, empCode
        )
    }

    // ─────────────────────────────────
    // ✅ Render detail
    // ─────────────────────────────────
    private fun renderDetail(detail: MonthlyEmployeeDetail) {

        // ✅ Count bar
        binding.tvCountPresent.text = detail.presentDays.toString()
        binding.tvCountAbsent.text  = detail.absentDays.toString()
        binding.tvCountHoliday.text = detail.holidayDays.toString()
        binding.tvCountWeekOff.text = detail.weekOffDays.toString()

        // ✅ Update tab labels
        binding.tabPresent.text   = "✅ Present (${detail.presentDays})"
        binding.tabAbsent.text    = "❌ Absent (${detail.absentDays})"
        binding.tabHolidayWO.text = "🏖️ H/WO (${detail.holidayDays + detail.weekOffDays})"

        // ✅ Employee card
        val initials = detail.empName.trim().split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2).joinToString("")
        binding.tvAvatar.text   = initials
        binding.tvEmpName2.text = detail.empName
        binding.tvEmpInfo.text  = "$empCode · $deptName · $desgName"
        binding.tvShiftTag.text = shiftName

        // ✅ Attendance %
        binding.tvAttendancePct.text = "${detail.attendancePercent}%"
        val pctColor = when {
            detail.attendancePercent >= 80 -> "#27500A"
            detail.attendancePercent >= 50 -> "#633806"
            else                           -> "#E24B4A"
        }
        binding.tvAttendancePct.setTextColor(Color.parseColor(pctColor))

        // ✅ Render dates
        renderDates(detail)
    }

    // ─────────────────────────────────
    // ✅ Render date pills
    // ─────────────────────────────────
    private fun renderDates(detail: MonthlyEmployeeDetail?) {
        detail ?: return
        binding.layoutDatePills.removeAllViews()

        val dates = when (currentType) {
            "PRESENT"    -> detail.presentDates
            "ABSENT"     -> detail.absentDates
            "HOLIDAY_WO" -> detail.holidayDates + detail.weekOffDates
            else         -> detail.presentDates
        }

        val bgRes = when (currentType) {
            "PRESENT"    -> R.drawable.bg_card_working
            "ABSENT"     -> R.drawable.bg_badge_red_light
            "HOLIDAY_WO" -> R.drawable.bg_badge_purple
            else         -> R.drawable.bg_card_working
        }
        val textColor = when (currentType) {
            "PRESENT"    -> "#27500A"
            "ABSENT"     -> "#E24B4A"
            "HOLIDAY_WO" -> "#3C3489"
            else         -> "#27500A"
        }
        val subColor = when (currentType) {
            "PRESENT"    -> "#639922"
            "ABSENT"     -> "#E24B4A"
            "HOLIDAY_WO" -> "#7E6FD8"
            else         -> "#639922"
        }

        // ✅ Section label
        binding.tvDateSectionLabel.text = when (currentType) {
            "PRESENT"    -> "✅ Present Dates (${detail.presentDays})"
            "ABSENT"     -> "❌ Absent Dates (${detail.absentDays})"
            "HOLIDAY_WO" -> "🏖️ Holiday + Week Off (${detail.holidayDays + detail.weekOffDays})"
            else         -> ""
        }
        binding.tvDateSectionLabel.setTextColor(
            Color.parseColor(textColor)
        )

        if (dates.isEmpty()) {
            val tv = TextView(this)
            tv.text      = "No dates in this category 🎉"
            tv.textSize  = 9f
            tv.setTextColor(Color.parseColor("#BDBDBD"))
            binding.layoutDatePills.addView(tv)
            return
        }

        dates.sorted().forEach { date ->
            val pill = LayoutInflater.from(this).inflate(
                R.layout.item_date_pill,
                binding.layoutDatePills,
                false
            )
            pill.setBackgroundResource(bgRes)
            pill.findViewById<TextView>(R.id.tvPillDate).apply {
                text = formatDate(date)         // "1 Jun"
                setTextColor(Color.parseColor(textColor))
            }
            pill.findViewById<TextView>(R.id.tvPillDay).apply {
                text = getDayName(date)         // "Mon"
                setTextColor(Color.parseColor(subColor))
            }
            binding.layoutDatePills.addView(pill)
        }
    }

    // ─────────────────────────────────
    // ✅ Helpers
    // ─────────────────────────────────
    private fun formatDate(date: String): String {
        return try {
            val inp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val out = SimpleDateFormat("d MMM", Locale.getDefault())
            out.format(inp.parse(date) ?: Date())
        } catch (e: Exception) { date }
    }

    private fun getDayName(date: String): String {
        return try {
            val inp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val out = SimpleDateFormat("EEE", Locale.getDefault())
            out.format(inp.parse(date) ?: Date())
        } catch (e: Exception) { "" }
    }
}