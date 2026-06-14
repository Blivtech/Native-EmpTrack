package com.blivtech.emptrack.ui.contract

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.ContractEntryEntity
import com.blivtech.emptrack.databinding.ActivityContractWageBinding
import com.blivtech.emptrack.databinding.ItemContractEntryHomeBinding
import com.blivtech.emptrack.utils.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ContractWageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContractWageBinding
    private val viewModel: ContractWageViewModel by viewModels()

    @Inject lateinit var preferenceManager: PreferenceManager

    private val btCode      by lazy { intent.getStringExtra("btCode") ?: "" }
    private val companyCode by lazy { intent.getStringExtra("companyCode") ?: "" }
    private val companyName by lazy { intent.getStringExtra("companyName") ?: "" }

    private var currentMonth = SimpleDateFormat(
        "yyyy-MM", Locale.getDefault()
    ).format(Date())

    private var allEntries = listOf<ContractEntryEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContractWageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupClickListeners()
        observeData()

        // ✅ Init default units on first launch
        viewModel.initDefaultUnits(btCode)
    }

    // ─────────────────────────────────
    // ✅ Setup UI
    // ─────────────────────────────────
    private fun setupUI() {
        binding.tvCompanyName.text = companyName
        binding.tvMonth.text = formatMonthDisplay(currentMonth)
    }

    // ─────────────────────────────────
    // ✅ Click listeners
    // ─────────────────────────────────
    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        // ✅ Previous month
        binding.btnPrevMonth.setOnClickListener {
            currentMonth = prevMonth(currentMonth)
            binding.tvMonth.text = formatMonthDisplay(currentMonth)
            loadEntries()
        }

        // ✅ Next month — no future months
        binding.btnNextMonth.setOnClickListener {
            val next = nextMonth(currentMonth)
            val thisMonth = SimpleDateFormat(
                "yyyy-MM", Locale.getDefault()
            ).format(Date())
            if (next > thisMonth) return@setOnClickListener
            currentMonth = next
            binding.tvMonth.text = formatMonthDisplay(currentMonth)
            loadEntries()
        }

        // ✅ FAB → Add entry
        binding.fabAdd.setOnClickListener { openAddEntry() }

        // ✅ Manage product rates
        binding.btnManageRates.setOnClickListener {
            startActivity(
                Intent(this, ContractProductListActivity::class.java).apply {
                    putExtra("btCode", btCode)
                    putExtra("companyCode", companyCode)
                }
            )
        }
    }

    // ─────────────────────────────────
    // ✅ Observe data
    // ─────────────────────────────────
    private fun observeData() {
        loadEntries()
    }

    private fun loadEntries() {
        viewModel.getEntriesByMonth(btCode, companyCode, currentMonth)
            .observe(this) { entries ->
                allEntries = entries
                renderEntries(entries)
                renderSummary(entries)
            }
    }

    // ─────────────────────────────────
    // ✅ Render daily entries
    // ─────────────────────────────────
    private fun renderEntries(entries: List<ContractEntryEntity>) {
        binding.layoutEntries.removeAllViews()

        if (entries.isEmpty()) {
            binding.layoutEmpty.visibility      = View.VISIBLE
            binding.layoutEntries.visibility    = View.GONE
            binding.layoutSummaryCard.visibility = View.GONE
            return
        }

        binding.layoutEmpty.visibility      = View.GONE
        binding.layoutEntries.visibility    = View.VISIBLE
        binding.layoutSummaryCard.visibility = View.VISIBLE

        // ✅ Group by date + shift
        val grouped = entries.groupBy { "${it.entryDate}_${it.shiftCode}" }

        grouped.forEach { (_, group) ->
            val first = group.first()
            val card  = ItemContractEntryHomeBinding.inflate(
                LayoutInflater.from(this), binding.layoutEntries, false
            )

            card.tvDate.text         = formatDateDisplay(first.entryDate)
            card.tvShift.text        = first.shiftName
            card.tvProductCount.text = "${group.size} product${if (group.size > 1) "s" else ""}"
            card.tvAmount.text       = "₹${String.format("%.0f", group.sumOf { it.totalAmount })}"

            // ✅ Edit button
            card.btnEdit.setOnClickListener {
                openEditEntry(first.entryDate, first.shiftCode)
            }

            binding.layoutEntries.addView(card.root)
        }
    }

    // ─────────────────────────────────
    // ✅ Render monthly summary
    // ─────────────────────────────────
    private fun renderSummary(entries: List<ContractEntryEntity>) {
        if (entries.isEmpty()) return

        val totalAmount = entries.sumOf { it.totalAmount }
        val summary     = viewModel.buildMonthlySummary(entries)

        binding.tvMonthTotal.text   = "₹${String.format("%.0f", totalAmount)}"
        binding.tvSummaryMonth.text = formatMonthDisplay(currentMonth)

        // ✅ Build summary table rows
        binding.layoutSummaryRows.removeAllViews()

        summary.forEach { item ->
            val row = LayoutInflater.from(this).inflate(
                R.layout.item_contract_summary_row,
                binding.layoutSummaryRows,
                false
            )
            row.findViewById<TextView>(R.id.tvSumProduct).text = item.productName
            row.findViewById<TextView>(R.id.tvSumWork).text    = "${item.workName} · ${item.unit}"
            row.findViewById<TextView>(R.id.tvSumQty).text     = String.format("%.1f", item.totalQty)
            row.findViewById<TextView>(R.id.tvSumRate).text    = "₹${item.ratePerUnit}"
            row.findViewById<TextView>(R.id.tvSumAmount).text  = "₹${String.format("%.0f", item.totalAmount)}"

            binding.layoutSummaryRows.addView(row)
        }
    }

    // ─────────────────────────────────
    // ✅ Navigation
    // ─────────────────────────────────
    private fun openAddEntry() {
        startActivity(
            Intent(this, AddContractEntryActivity::class.java).apply {
                putExtra("btCode",      btCode)
                putExtra("companyCode", companyCode)
                putExtra("companyName", companyName)
            }
        )
    }

    private fun openEditEntry(date: String, shiftCode: String) {
        startActivity(
            Intent(this, AddContractEntryActivity::class.java).apply {
                putExtra("btCode",       btCode)
                putExtra("companyCode",  companyCode)
                putExtra("companyName",  companyName)
                putExtra("editDate",     date)
                putExtra("editShiftCode", shiftCode)
                putExtra("isEdit",       true)
            }
        )
    }

    // ─────────────────────────────────
    // ✅ Helpers
    // ─────────────────────────────────
    private fun formatMonthDisplay(month: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val out = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            out.format(sdf.parse(month) ?: Date())
        } catch (e: Exception) { month }
    }

    private fun formatDateDisplay(date: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val out = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            out.format(sdf.parse(date) ?: Date())
        } catch (e: Exception) { date }
    }

    private fun prevMonth(month: String): String {
        val cal = Calendar.getInstance()
        cal.time = SimpleDateFormat(
            "yyyy-MM", Locale.getDefault()
        ).parse(month) ?: Date()
        cal.add(Calendar.MONTH, -1)
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(cal.time)
    }

    private fun nextMonth(month: String): String {
        val cal = Calendar.getInstance()
        cal.time = SimpleDateFormat(
            "yyyy-MM", Locale.getDefault()
        ).parse(month) ?: Date()
        cal.add(Calendar.MONTH, 1)
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(cal.time)
    }
}