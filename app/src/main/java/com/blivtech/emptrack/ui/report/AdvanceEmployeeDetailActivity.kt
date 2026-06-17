package com.blivtech.emptrack.ui.report

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.model.AdvanceEmployeeDetail
import com.blivtech.emptrack.data.model.AdvanceEntry
import com.blivtech.emptrack.databinding.ActivityAdvanceEmployeeDetailBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AdvanceEmployeeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdvanceEmployeeDetailBinding
    private val viewModel: AdvanceEmployeeDetailViewModel by viewModels()

    private val btCode      by lazy { intent.getStringExtra("btCode")      ?: "" }
    private val companyCode by lazy { intent.getStringExtra("companyCode") ?: "" }
    private val month       by lazy { intent.getStringExtra("month")       ?: "" }
    private val monthLabel  by lazy { intent.getStringExtra("monthLabel")  ?: "" }
    private val empCode     by lazy { intent.getStringExtra("empCode")     ?: "" }
    private val empName     by lazy { intent.getStringExtra("empName")     ?: "" }
    private val deptName    by lazy { intent.getStringExtra("deptName")    ?: "" }
    private val desgName    by lazy { intent.getStringExtra("desgName")    ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdvanceEmployeeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeData()
    }

    // ─────────────────────────────────
    // ✅ Setup
    // ─────────────────────────────────
    private fun setupUI() {
        binding.tvEmpTitle.text    = empName
        binding.tvEmpSubtitle.text = "$empCode · $monthLabel"
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

        viewModel.loadDetail(btCode, companyCode, month, empCode)
    }

    // ─────────────────────────────────
    // ✅ Render detail
    // ─────────────────────────────────
    private fun renderDetail(detail: AdvanceEmployeeDetail) {

        // ✅ Summary strip
        binding.tvEntryCount.text  = detail.entryCount.toString()
        binding.tvTotalAmount.text = viewModel.formatAmount(detail.totalAmount)

        // ✅ Employee card
        val initials = detail.empName.trim().split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2).joinToString("")
        binding.tvAvatar.text   = initials
        binding.tvEmpName2.text = detail.empName
        binding.tvEmpInfo.text  = "$empCode · ${detail.deptName} · ${detail.desgName}"
        binding.tvShiftTag.text = detail.shiftName

//        // ✅ Render entries
//        renderEntries(detail.entries)

        // ✅ Total box
        binding.tvTotalBoxLabel.text  = "Total — $monthLabel"
        binding.tvTotalBoxAmount.text = viewModel.formatAmount(detail.totalAmount)
    }

//    // ─────────────────────────────────
//    // ✅ Render entry rows
//    // ─────────────────────────────────
//    private fun renderEntries(entries: List<AdvanceEntry>) {
//        binding.layoutEntries.removeAllViews()
//
//        if (entries.isEmpty()) {
//            binding.tvNoEntries.visibility    = View.VISIBLE
//            binding.layoutEntries.visibility  = View.GONE
//            return
//        }
//
//        binding.tvNoEntries.visibility    = View.GONE
//        binding.layoutEntries.visibility  = View.VISIBLE
//
//        entries.forEach { entry ->
//            val row = LayoutInflater.from(this).inflate(
//                R.layout.item_advance_entry_row,
//                binding.layoutEntries,
//                false
//            )
//
//            // ✅ Date box
//            val dateParts = formatDateParts(entry.date)
//            row.findViewById<TextView>(R.id.tvDateDay).text   = dateParts.first
//            row.findViewById<TextView>(R.id.tvDateMonth).text = dateParts.second
//
//            // ✅ Entry info
//            row.findViewById<TextView>(R.id.tvEntryTitle).text =
//                "Advance Payment"
//            row.findViewById<TextView>(R.id.tvEntryNote).text =
//                entry.reason.ifEmpty { "No reason provided" }
//
//            // ✅ Amount
//            row.findViewById<TextView>(R.id.tvEntryAmount).text =
//                viewModel.formatAmount(entry.amount)
//
//            binding.layoutEntries.addView(row)
//        }
//    }

    // ─────────────────────────────────
    // ✅ Format date → "05" + "Jun"
    // ─────────────────────────────────
    private fun formatDateParts(date: String): Pair<String, String> {
        return try {
            val inp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val d   = SimpleDateFormat("dd", Locale.getDefault())
            val m   = SimpleDateFormat("MMM", Locale.getDefault())
            val parsed = inp.parse(date) ?: Date()
            d.format(parsed) to m.format(parsed)
        } catch (e: Exception) {
            date to ""
        }
    }
}