package com.blivtech.emptrack.ui.report

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.model.BonusEntryDto
import com.blivtech.emptrack.databinding.ActivityBonusReportBinding
import com.blivtech.emptrack.databinding.DialogEditBonusBinding
import com.blivtech.emptrack.utils.PreferenceManager
import com.blivtech.emptrack.utils.Resource
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BonusReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBonusReportBinding
    private val viewModel: BonusReportViewModel by viewModels()

    @Inject lateinit var preferenceManager: PreferenceManager

    private var btCode      = ""
    private var companyCode = ""
    private var companyName = ""

    private val apiDateFmt     = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBonusReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            btCode      = preferenceManager.btCode.first()
            companyCode = preferenceManager.selectedCompanyCode.first()
            companyName = preferenceManager.selectedCompanyName.first()
            setupUI()
            observeData()
            loadData()
        }
    }

    private fun setupUI() {
        binding.tvCompanyName.text = companyName
        binding.ivBack.setOnClickListener { finish() }
        binding.btnPrevMonth.setOnClickListener { viewModel.prevMonth(); loadData() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth(); loadData() }
    }

    private fun loadData() { viewModel.loadBonusList(btCode, companyCode) }

    private fun observeData() {
        viewModel.monthLabel.observe(this) { binding.tvMonthLabel.text = it }

        viewModel.loading.observe(this) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error ?: return@observe
            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            viewModel.resetError()
        }

        viewModel.monthlyData.observe(this) { data ->
            data ?: return@observe
            binding.tvSummary.text =
                "${data.totalEntries} entries · ₹${"%,.0f".format(data.totalAmount)}"
            renderList(data.entries)
        }

        viewModel.actionState.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Snackbar.make(binding.root, "Saved ✅", Snackbar.LENGTH_SHORT).show()
                    viewModel.resetActionState()
                    loadData()
                }
                is Resource.Error -> {
                    Snackbar.make(binding.root, resource.message, Snackbar.LENGTH_LONG).show()
                    viewModel.resetActionState()
                }
                else -> {}
            }
        }
    }

    private fun renderList(entries: List<BonusEntryDto>) {
        binding.layoutEntries.removeAllViews()

        if (entries.isEmpty()) {
            binding.layoutEmpty.visibility   = View.VISIBLE
            binding.layoutEntries.visibility = View.GONE
            return
        }
        binding.layoutEmpty.visibility   = View.GONE
        binding.layoutEntries.visibility = View.VISIBLE

        entries.forEach { entry ->
            val row = LayoutInflater.from(this).inflate(
                R.layout.item_bonus_entry_row, binding.layoutEntries, false
            )
            val initials = entry.empName.trim().split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")

            row.findViewById<TextView>(R.id.tvAvatar).text    = initials
            row.findViewById<TextView>(R.id.tvEmpName).text   = entry.empName
            row.findViewById<TextView>(R.id.tvEmpMeta).text   = "${entry.empCode} · ${entry.deptName}"
            row.findViewById<TextView>(R.id.tvBonusType).text = entry.bonusType
            row.findViewById<TextView>(R.id.tvDate).text      = formatDate(entry.bonusDate)
            row.findViewById<TextView>(R.id.tvAmount).text    = "₹${"%,.0f".format(entry.amount)}"

            val remarksLayout = row.findViewById<View>(R.id.layoutRemarks)
            if (entry.remarks.isNotEmpty()) {
                remarksLayout.visibility = View.VISIBLE
                row.findViewById<TextView>(R.id.tvRemarks).text = entry.remarks
            } else {
                remarksLayout.visibility = View.GONE
            }

            row.findViewById<View>(R.id.ivEdit).setOnClickListener   { showEditDialog(entry) }
            row.findViewById<View>(R.id.ivDelete).setOnClickListener { showDeleteConfirm(entry) }

            binding.layoutEntries.addView(row)
        }
    }

    private fun showEditDialog(entry: BonusEntryDto) {
        val dialogBinding = DialogEditBonusBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.tvEmpInfo.text = "${entry.empName} · ${entry.empCode}"

        var selectedDate = entry.bonusDate
        var selectedType = entry.bonusType

        dialogBinding.tvSelectedDate.text = formatDate(selectedDate)
        dialogBinding.etAmount.setText(entry.amount.toString())
        dialogBinding.etRemarks.setText(entry.remarks)

        val chips = mapOf(
            "Festival"    to dialogBinding.chipFestival,
            "Performance" to dialogBinding.chipPerformance,
            "Attendance"  to dialogBinding.chipAttendance
        )

        fun refreshChips() {
            chips.forEach { (type, chip) ->
                if (type == selectedType) {
                    chip.setBackgroundResource(R.drawable.bg_badge_amber)
                    chip.setTextColor(Color.parseColor("#633806"))
                } else {
                    chip.setBackgroundResource(R.drawable.bg_edit_field_normal)
                    chip.setTextColor(Color.parseColor("#757575"))
                }
            }
        }
        refreshChips()

        chips.forEach { (type, chip) ->
            chip.setOnClickListener {
                selectedType = type
                refreshChips()
            }
        }

        dialogBinding.layoutDate.setOnClickListener {
            val cal = Calendar.getInstance().apply {
                time = apiDateFmt.parse(selectedDate) ?: Date()
            }
            DatePickerDialog(this, { _, y, m, d ->
                val newCal = Calendar.getInstance().apply { set(y, m, d) }
                selectedDate = apiDateFmt.format(newCal.time)
                dialogBinding.tvSelectedDate.text = formatDate(selectedDate)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .apply { datePicker.maxDate = System.currentTimeMillis() }
                .show()
        }

        dialogBinding.ivClose.setOnClickListener   { dialog.dismiss() }
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnSave.setOnClickListener {
            val amount = dialogBinding.etAmount.text.toString().toDoubleOrNull()
            if (amount == null || amount <= 0) {
                dialogBinding.etAmount.error = "Enter a valid amount"
                return@setOnClickListener
            }
            val remarks = dialogBinding.etRemarks.text.toString().trim()
            viewModel.updateBonus(
                entry     = entry,
                bonusDate = selectedDate,
                bonusType = selectedType,
                amount    = amount,
                remarks   = remarks
            )
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirm(entry: BonusEntryDto) {
        AlertDialog.Builder(this)
            .setTitle("Delete Bonus?")
            .setMessage(
                "Employee : ${entry.empName}\n" +
                        "Date     : ${formatDate(entry.bonusDate)}\n" +
                        "Type     : ${entry.bonusType}\n" +
                        "Amount   : ₹${"%,.0f".format(entry.amount)}\n\n" +
                        "This action cannot be undone."
            )
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteBonus(entry) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatDate(date: String): String {
        return try { displayDateFmt.format(apiDateFmt.parse(date) ?: Date()) }
        catch (e: Exception) { date }
    }
}