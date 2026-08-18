package com.blivtech.emptrack.ui.report

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.model.ReportItem
import com.blivtech.emptrack.databinding.FragmentReportBinding
import com.blivtech.emptrack.ui.report.adapter.ReportAdapter
import com.blivtech.emptrack.utils.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReportsFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private lateinit var reportAdapter: ReportAdapter

    private var companyName = ""
    private var companyCode = ""
    private var btCode      = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            btCode      = preferenceManager.btCode.first()
            companyCode = preferenceManager.selectedCompanyCode.first()
            companyName = preferenceManager.selectedCompanyName.first()

            setupUI()
            setupRecyclerView()
            loadReports()
        }
    }

    private fun setupUI() {
        binding.tvCompanyName.text = companyName
    }

    private fun setupRecyclerView() {
        reportAdapter = ReportAdapter { item -> openReport(item) }
        binding.rvReports.apply {
            adapter       = reportAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
    }

    private fun loadReports() {
        val reports = listOf(
            ReportItem(
                id = "DAILY", name = "Daily Report",
                subtitle = "Shift-wise attendance by date",
                category = "ATTENDANCE", tag = "",
                iconRes = R.drawable.ic_rep_daily,
                iconBgColor = "#DBEAFE", iconTintColor = "#2563EB",
                destination = DailyReportActivity::class.java
            ),
            ReportItem(
                id = "WEEKLY", name = "Weekly Report",
                subtitle = "Shift-wise summary by week",
                category = "ATTENDANCE", tag = "",
                iconRes = R.drawable.ic_rep_weekly,
                iconBgColor = "#DCFCE7", iconTintColor = "#16A34A",
                destination = WeeklyReportActivity::class.java
            ),
            ReportItem(
                id = "MONTHLY", name = "Monthly Report",
                subtitle = "Monthly attendance summary",
                category = "ATTENDANCE", tag = "",
                iconRes = R.drawable.ic_rep_monthly,
                iconBgColor = "#EEF0FF", iconTintColor = "#4F46E5",
                destination = MonthlyReportActivity::class.java
            ),
            ReportItem(
                id = "ADVANCE", name = "Advance Report",
                subtitle = "Advance summary",
                category = "PAY", tag = "",
                iconRes = R.drawable.ic_rep_advance,
                iconBgColor = "#FEF3E2", iconTintColor = "#D97706",
                destination = AdvanceReportActivity::class.java
            ),
            ReportItem(
                id = "OVERTIME", name = "Overtime Report",
                subtitle = "Overtime summary",
                category = "PAY", tag = "",
                iconRes = R.drawable.ic_rep_overtime,
                iconBgColor = "#CCFBF1", iconTintColor = "#0D9488",
                destination = OvertimeReportActivity::class.java
            ),
            ReportItem(
                id = "BONUS", name = "Bonus Report",
                subtitle = "Bonus summary",
                category = "PAY", tag = "",
                iconRes = R.drawable.ic_rep_bonus,
                iconBgColor = "#FFE4E6", iconTintColor = "#E11D48",
                destination = BonusReportActivity::class.java
            )
        )
        reportAdapter.submitList(reports)
    }

    private fun openReport(item: ReportItem) {
        startActivity(
            Intent(requireContext(), item.destination).apply {
                putExtra("btCode",      btCode)
                putExtra("companyCode", companyCode)
                putExtra("companyName", companyName)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
