package com.blivtech.emptrack.ui.report

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.model.ShiftAttendanceSummary
import com.blivtech.emptrack.databinding.ActivityDailyReportBinding
import com.blivtech.emptrack.utils.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class DailyReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDailyReportBinding
    private val viewModel: DailyReportViewModel by viewModels()
    @Inject
    lateinit var preferenceManager: PreferenceManager


    private var btCode  = ""
    private var companyCode= ""
    private var companyName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launch {
            btCode = preferenceManager.btCode.first()
            companyCode = preferenceManager.selectedCompanyCode.first()
            companyName = preferenceManager.selectedCompanyName.first()
            setupUI()
            setupClickListeners()
            observeData()
        }

    }

    // ─────────────────────────────────
    // ✅ Setup
    // ─────────────────────────────────
    private fun setupUI() {
        binding.tvCompanyName.text = companyName
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        // ✅ Date navigation
        binding.btnPrevDay.setOnClickListener {
            viewModel.prevDay()
            loadData()
        }
        binding.btnNextDay.setOnClickListener {
            viewModel.nextDay()
            loadData()
        }

        // ✅ Date tap → date picker
        binding.tvDate.setOnClickListener { showDatePicker() }

        // ✅ Download
        binding.btnDownload.setOnClickListener {
            // TODO: PDF export
        }
    }

    private fun observeData() {
        viewModel.displayDate.observe(this) { date ->
            binding.tvDate.text = date
        }
        viewModel.summaries.observe(this) { summaries ->
            renderSummaries(summaries)
            updateTotals(summaries)
        }
        loadData()
    }

    private fun loadData() {
        viewModel.loadSummaries(btCode, companyCode)
    }

    // ─────────────────────────────────
    // ✅ Render overall totals
    // ─────────────────────────────────
    private fun updateTotals(summaries: List<ShiftAttendanceSummary>) {
        val totalPresent = summaries.sumOf { it.presentCount }
        val totalLeave   = summaries.sumOf { it.leaveCount }
        val totalAll     = summaries.sumOf { it.totalCount }

        binding.tvTotalPresent.text = if (totalPresent > 0)
            totalPresent.toString() else "—"
        binding.tvTotalLeave.text = if (totalLeave > 0)
            totalLeave.toString() else "—"

        // ✅ Check if any not submitted
        val pendingCount = summaries.count { it.submittedAt == null }
        if (pendingCount > 0) {
            binding.tvTotalPending.text = "$pendingCount shift${
                if (pendingCount > 1) "s" else ""
            } pending"
            binding.layoutPendingBadge.visibility = View.VISIBLE
        } else {
            binding.layoutPendingBadge.visibility = View.GONE
            binding.tvTotalPending.text = totalAll.toString()
        }
    }

    // ─────────────────────────────────
    // ✅ Render shift cards
    // ─────────────────────────────────
    private fun renderSummaries(summaries: List<ShiftAttendanceSummary>) {
        binding.layoutShiftCards.removeAllViews()

        if (summaries.isEmpty()) {
            binding.layoutEmpty.visibility      = View.VISIBLE
            binding.layoutShiftCards.visibility = View.GONE
            return
        }

        binding.layoutEmpty.visibility      = View.GONE
        binding.layoutShiftCards.visibility = View.VISIBLE

        summaries.forEach { summary ->
            val card = LayoutInflater.from(this).inflate(
                R.layout.item_shift_report_card,
                binding.layoutShiftCards, false
            )

            // ✅ Shift header
            card.findViewById<TextView>(R.id.tvShiftEmoji).text =
                viewModel.shiftEmoji(summary.shiftName)
            card.findViewById<TextView>(R.id.tvShiftName).text =
                summary.shiftName
            card.findViewById<TextView>(R.id.tvShiftTime).text =
                "${summary.startTime} – ${summary.endTime}"

            // ✅ Submitted badge
            val tvSubmitBadge = card.findViewById<TextView>(R.id.tvSubmitBadge)
            if (summary.submittedAt != null) {
                tvSubmitBadge.text = "✓ Submitted"
                tvSubmitBadge.setBackgroundResource(R.drawable.bg_badge_green)
                tvSubmitBadge.setTextColor(
                    resources.getColor(R.color.green_dark, theme)
                )
            } else {
                tvSubmitBadge.text = "⏳ Pending"
                tvSubmitBadge.setBackgroundResource(R.drawable.bg_badge_amber)
                tvSubmitBadge.setTextColor(
                    resources.getColor(R.color.amber_dark, theme)
                )
            }

            // ✅ Meta — date
            card.findViewById<TextView>(R.id.tvMetaDate).text =
                formatDisplayDate(summary.attendanceDate)

            // ✅ Meta — submitted time
            val tvMetaSubmit = card.findViewById<TextView>(R.id.tvMetaSubmit)
            if (summary.submittedAt != null) {
                tvMetaSubmit.text = "✓ ${formatSubmittedAt(summary.submittedAt)}"
                tvMetaSubmit.setTextColor(
                    resources.getColor(R.color.green_dark, theme)
                )
            } else {
                tvMetaSubmit.text = "Not submitted yet"
                tvMetaSubmit.setTextColor(
                    resources.getColor(R.color.amber_dark, theme)
                )
            }

            // ✅ Counts
            val tvPresent = card.findViewById<TextView>(R.id.tvPresentCount)
            val tvLeave   = card.findViewById<TextView>(R.id.tvLeaveCount)
            val tvTotal   = card.findViewById<TextView>(R.id.tvTotalCount)
            val pillPresent = card.findViewById<LinearLayout>(R.id.pillPresent)
            val pillLeave   = card.findViewById<LinearLayout>(R.id.pillLeave)

            if (summary.submittedAt != null) {
                tvPresent.text = summary.presentCount.toString()
                tvLeave.text   = summary.leaveCount.toString()
                tvTotal.text   = summary.totalCount.toString()
                pillPresent.alpha = 1f
                pillLeave.alpha   = 1f

                // ✅ Tap present → employee list
                pillPresent.setOnClickListener {
                    openEmployeeList(summary, "PRESENT")
                }

                // ✅ Tap leave → employee list
                pillLeave.setOnClickListener {
                    openEmployeeList(summary, "LEAVE")
                }
            } else {
                // ✅ Not submitted — disable taps
                tvPresent.text = "—"
                tvLeave.text   = "—"
                tvTotal.text   = summary.totalCount.toString()
                pillPresent.alpha = 0.4f
                pillLeave.alpha   = 0.4f
                pillPresent.isClickable = false
                pillLeave.isClickable   = false
            }

            binding.layoutShiftCards.addView(card)
        }
    }

    // ─────────────────────────────────
    // ✅ Navigation
    // ─────────────────────────────────
    private fun openEmployeeList(
        summary: ShiftAttendanceSummary,
        type: String    // PRESENT or LEAVE
    ) {
        startActivity(
            Intent(this, ShiftEmployeeListActivity::class.java).apply {
                putExtra("btCode",        btCode)
                putExtra("companyCode",   companyCode)
                putExtra("companyName",   companyName)
                putExtra("shiftCode",     summary.shiftCode)
                putExtra("shiftName",     summary.shiftName)
                putExtra("shiftTime",     "${summary.startTime} – ${summary.endTime}")
                putExtra("shiftEmoji",    viewModel.shiftEmoji(summary.shiftName))
                putExtra("attendanceDate", summary.attendanceDate)
                putExtra("type",          type)
                putExtra("count",         if (type == "PRESENT")
                    summary.presentCount else summary.leaveCount
                )
            }
        )
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selected = String.format("%04d-%02d-%02d", year, month + 1, day)
                viewModel.setSelectedDate(selected)
                loadData()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    // ─────────────────────────────────
    // ✅ Helpers
    // ─────────────────────────────────
    private fun formatDisplayDate(date: String): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val out = java.text.SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            out.format(sdf.parse(date) ?: java.util.Date())
        } catch (e: Exception) { date }
    }

    private fun formatSubmittedAt(dateTime: String): String {
        return try {
            val sdf = java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
            )
            val out = java.text.SimpleDateFormat(
                "d MMM yyyy · hh:mm a", Locale.getDefault()
            )
            out.format(sdf.parse(dateTime) ?: java.util.Date())
        } catch (e: Exception) { dateTime }
    }
}