package com.blivtech.emptrack.ui.report

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
import com.blivtech.emptrack.data.model.WeeklyShiftSummaryDto
import com.blivtech.emptrack.databinding.ActivityWeeklyReportBinding
import com.blivtech.emptrack.utils.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WeeklyReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeeklyReportBinding
    private val viewModel: WeeklyReportViewModel by viewModels()

    @Inject
    lateinit var preferenceManager: PreferenceManager


    private var btCode  = ""
    private var companyCode= ""
    private var companyName = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeeklyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            btCode = preferenceManager.btCode.first()
            companyCode = preferenceManager.selectedCompanyCode.first()
            companyName = preferenceManager.selectedCompanyName.first()
        setupUI()
        setupClickListeners()
        observeData()}
    }

    // ─────────────────────────────────
    // ✅ Setup
    // ─────────────────────────────────
    private fun setupUI() {
        binding.tvCompanyName.text = companyName
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.btnPrevWeek.setOnClickListener {
            viewModel.prevWeek()
            loadData()
        }

        binding.btnNextWeek.setOnClickListener {
            viewModel.nextWeek()
            loadData()
        }

        binding.btnDownload.setOnClickListener {
            // TODO: PDF export
        }
    }

    private fun observeData() {
        // ✅ Week label
        viewModel.weekLabel.observe(this) {
            binding.tvWeekLabel.text = it
        }
        viewModel.weekSubLabel.observe(this) {
            binding.tvWeekSubLabel.text = it
        }

        // ✅ Loading
        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }

        // ✅ Error
        viewModel.error.observe(this) { error ->
            error ?: return@observe
            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            viewModel.resetError()
        }

        // ✅ Report data
        viewModel.report.observe(this) { report ->
            report ?: return@observe

            // ✅ Overall strip
            binding.tvTotalPresent.text = report.totalPresent.toString()
            binding.tvTotalAbsent.text  = report.totalAbsent.toString()
            binding.tvWorkDays.text     = report.workDays.toString()

            // ✅ Shift cards
            renderShiftCards(report.shifts)
        }

        loadData()
    }

    private fun loadData() {
        viewModel.loadWeeklyReport(btCode, companyCode)
    }

    // ─────────────────────────────────
    // ✅ Render shift cards
    // ─────────────────────────────────
    private fun renderShiftCards(shifts: List<WeeklyShiftSummaryDto>) {
        binding.layoutShiftCards.removeAllViews()

        if (shifts.isEmpty()) {
            binding.layoutEmpty.visibility      = View.VISIBLE
            binding.layoutShiftCards.visibility = View.GONE
            return
        }

        binding.layoutEmpty.visibility      = View.GONE
        binding.layoutShiftCards.visibility = View.VISIBLE

        shifts.forEach { shift ->
            val card = LayoutInflater.from(this).inflate(
                R.layout.item_weekly_shift_card,
                binding.layoutShiftCards,
                false
            )

            // ✅ Shift info
            card.findViewById<TextView>(R.id.tvShiftEmoji).text =
                viewModel.shiftEmoji(shift.shiftName)
            card.findViewById<TextView>(R.id.tvShiftName).text =
                shift.shiftName
            card.findViewById<TextView>(R.id.tvShiftTime).text =
                "${shift.startTime} – ${shift.endTime}"

            // ✅ Submitted days badge
            val tvBadge = card.findViewById<TextView>(R.id.tvSubmitBadge)
            tvBadge.text = "${shift.submittedDays}/${shift.totalDays} days"
            if (shift.submittedDays == shift.totalDays) {
                tvBadge.setBackgroundResource(R.drawable.bg_badge_green)
                tvBadge.setTextColor(
                    resources.getColor(R.color.green_dark, theme)
                )
            } else {
                tvBadge.setBackgroundResource(R.drawable.bg_badge_amber)
                tvBadge.setTextColor(
                    resources.getColor(R.color.amber_dark, theme)
                )
            }

            // ✅ Week range
            card.findViewById<TextView>(R.id.tvWeekRange).text =
                "${viewModel.weekSubLabel.value}"

            // ✅ Count pills
            card.findViewById<TextView>(R.id.tvTotalCount).text =
                shift.totalEmployees.toString()
            card.findViewById<TextView>(R.id.tvPresentCount).text =
                shift.totalPresent.toString()
            card.findViewById<TextView>(R.id.tvAbsentCount).text =
                shift.totalAbsent.toString()

            // ✅ Tap present → employee list
            card.findViewById<LinearLayout>(R.id.pillPresent)
                .setOnClickListener {
                    openShiftEmployeeList(shift, "PRESENT")
                }

            // ✅ Tap absent → employee list
            card.findViewById<LinearLayout>(R.id.pillAbsent)
                .setOnClickListener {
                    openShiftEmployeeList(shift, "ABSENT")
                }

            binding.layoutShiftCards.addView(card)
        }
    }

    // ─────────────────────────────────
    // ✅ Open shift employee list
    // ─────────────────────────────────
    private fun openShiftEmployeeList(
        shift: WeeklyShiftSummaryDto,
        type: String
    ) {
        startActivity(
            Intent(this, WeeklyShiftEmployeeListActivity::class.java).apply {
                putExtra("btCode",        btCode)
                putExtra("companyCode",   companyCode)
                putExtra("companyName",   companyName)
                putExtra("shiftCode",     shift.shiftCode)
                putExtra("shiftName",     shift.shiftName)
                putExtra("shiftEmoji",    viewModel.shiftEmoji(shift.shiftName))
                putExtra("shiftTime",     "${shift.startTime} – ${shift.endTime}")
                putExtra("weekStart",     viewModel.weekStart.value)
                putExtra("weekEnd",       viewModel.weekEnd.value)
                putExtra("weekLabel",     viewModel.weekLabel.value)
                putExtra("type",          type)
                putExtra("presentCount",  shift.totalPresent)
                putExtra("absentCount",   shift.totalAbsent)
                putExtra("totalCount",    shift.totalEmployees)
            }
        )
    }
}