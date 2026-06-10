package com.blivtech.emptrack.ui.entry

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.data.model.EntryMode
import com.blivtech.emptrack.data.model.EntryType
import com.blivtech.emptrack.databinding.ActivityAddEntryBinding
import com.blivtech.emptrack.databinding.DialogEmployeePickerBinding
import com.blivtech.emptrack.databinding.DialogShiftPickerBinding
import com.blivtech.emptrack.ui.entry.adapter.EmployeePickerAdapter
import com.blivtech.emptrack.utils.PreferenceManager
import com.blivtech.emptrack.utils.Resource
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AddEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEntryBinding
    private val viewModel: AddEntryViewModel by viewModels()

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private val btCode by lazy { intent.getStringExtra("btCode") ?: "" }
    private val companyCode by lazy { intent.getStringExtra("companyCode") ?: "" }
    private val companyName by lazy { intent.getStringExtra("companyName") ?: "" }

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    private val monthFmt = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val monthDisplayFmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    private var shifts = listOf<ShiftEntity>()
    private val bonusTypes = listOf("Performance", "Festival", "Incentive", "Special", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupTypeSelector()
        setupModeSelector()
        setupDatePicker()
        setupBonusTypes()
        observeData()

        viewModel.loadEmployees(companyCode)
        viewModel.getShifts(companyCode).observe(this) { list ->
            shifts = list
        }

        // ✅ Default — today
        val today = Calendar.getInstance()
        viewModel.selectedDate = dateFmt.format(today.time)
        binding.tvDate.text = displayFmt.format(today.time)

        // ✅ Default repay month — next month
        val nextMonth = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }
        viewModel.selectedRepayMonth = monthFmt.format(nextMonth.time)
        binding.tvRepayMonth.text = monthDisplayFmt.format(nextMonth.time)
    }

    // ─────────────────────────────────────
    // ✅ Setup UI
    // ─────────────────────────────────────
    private fun setupUI() {
        binding.ivBack.setOnClickListener { finish() }
        binding.tvCompanyName.text = companyName

        // ✅ Amount input
        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.amount = s?.toString()?.toDoubleOrNull() ?: 0.0
                updateSaveButton()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // ✅ OT hours input
        binding.etOtHours.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.otHours = s?.toString()?.toDoubleOrNull() ?: 0.0
                updateSaveButton()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // ✅ Remarks
        binding.etRemarks.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.remarks = s?.toString() ?: ""
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // ✅ Shift picker
        binding.layoutShift.setOnClickListener { showShiftPicker() }

        // ✅ Repay month picker
        binding.layoutRepayMonth.setOnClickListener { showRepayMonthPicker() }

        // ✅ Employee picker
        binding.layoutEmployee.setOnClickListener { showEmployeePicker() }
        binding.btnAddMore.setOnClickListener { showEmployeePicker() }

        // ✅ Save button
        binding.btnSave.setOnClickListener { viewModel.save(btCode, companyCode) }
    }

    // ─────────────────────────────────────
    // ✅ Type selector
    // ─────────────────────────────────────
    private fun setupTypeSelector() {
        listOf(
            binding.cardOvertime,
            binding.cardAdvance,
            binding.cardBonus
        ).forEachIndexed { index, card ->
            card.setOnClickListener {
                viewModel.entryType = when (index) {
                    0 -> EntryType.OVERTIME
                    1 -> EntryType.ADVANCE
                    else -> EntryType.BONUS
                }
                viewModel.selectedEmployees.clear()
                updateTypeUI()
                updateFormFields()
                updateSaveButton()
                refreshEmployeePills()
            }
        }
        updateTypeUI()
        updateFormFields()
    }

    private fun updateTypeUI() {
        // ✅ Reset all
        listOf(binding.cardOvertime, binding.cardAdvance, binding.cardBonus)
            .forEach { it.setBackgroundResource(R.drawable.bg_card_unselected) }

        // ✅ Highlight selected
        when (viewModel.entryType) {
            EntryType.OVERTIME -> {
                binding.cardOvertime.setBackgroundResource(R.drawable.bg_card_working)
                binding.topbar.setBackgroundColor(
                    android.graphics.Color.parseColor("#1A6B1A")
                )
            }
            EntryType.ADVANCE -> {
                binding.cardAdvance.setBackgroundResource(R.drawable.bg_card_weekoff)
                binding.topbar.setBackgroundColor(
                    android.graphics.Color.parseColor("#7A4A00")
                )
            }
            EntryType.BONUS -> {
                binding.cardBonus.setBackgroundResource(R.drawable.bg_card_holiday)
                binding.topbar.setBackgroundColor(
                    android.graphics.Color.parseColor("#2E2680")
                )
            }
        }
    }

    // ─────────────────────────────────────
    // ✅ Form fields visibility
    // ─────────────────────────────────────
    private fun updateFormFields() {
        // ✅ Shift — only for OT
        binding.layoutShiftField.visibility =
            if (viewModel.entryType == EntryType.OVERTIME) View.VISIBLE
            else View.GONE

        // ✅ OT Hours — only for OT
        binding.layoutOtHours.visibility =
            if (viewModel.entryType == EntryType.OVERTIME) View.VISIBLE
            else View.GONE

        // ✅ Repay month — only for Advance
        binding.layoutRepayMonthField.visibility =
            if (viewModel.entryType == EntryType.ADVANCE) View.VISIBLE
            else View.GONE

        // ✅ Bonus type — only for Bonus
        binding.layoutBonusType.visibility =
            if (viewModel.entryType == EntryType.BONUS) View.VISIBLE
            else View.GONE

        // ✅ Amount label
        binding.tvAmountLabel.text = when (viewModel.entryType) {
            EntryType.OVERTIME -> "OT Amount"
            EntryType.ADVANCE  -> "Advance Amount"
            EntryType.BONUS    -> "Bonus Amount"
        }
    }

    // ─────────────────────────────────────
    // ✅ Mode selector
    // ─────────────────────────────────────
    private fun setupModeSelector() {
        binding.cardSingle.setOnClickListener {
            viewModel.entryMode = EntryMode.SINGLE
            viewModel.selectedEmployees.clear()
            updateModeUI()
            refreshEmployeePills()
            updateSaveButton()
        }
        binding.cardMultiple.setOnClickListener {
            viewModel.entryMode = EntryMode.MULTIPLE
            viewModel.selectedEmployees.clear()
            updateModeUI()
            refreshEmployeePills()
            updateSaveButton()
        }
        updateModeUI()
    }

    private fun updateModeUI() {
        if (viewModel.entryMode == EntryMode.SINGLE) {
            binding.cardSingle.setBackgroundResource(R.drawable.bg_badge_blue)
            binding.cardMultiple.setBackgroundResource(R.drawable.bg_card_unselected)
            binding.layoutSingleEmployee.visibility = View.VISIBLE
            binding.layoutMultiEmployee.visibility  = View.GONE
        } else {
            binding.cardMultiple.setBackgroundResource(R.drawable.bg_badge_blue)
            binding.cardSingle.setBackgroundResource(R.drawable.bg_card_unselected)
            binding.layoutSingleEmployee.visibility = View.GONE
            binding.layoutMultiEmployee.visibility  = View.VISIBLE
        }
    }

    // ─────────────────────────────────────
    // ✅ Date picker
    // ─────────────────────────────────────
    private fun setupDatePicker() {
        binding.layoutDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    cal.set(year, month, day)
                    viewModel.selectedDate = dateFmt.format(cal.time)
                    binding.tvDate.text = displayFmt.format(cal.time)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).apply {
                // ✅ Block future dates
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }
    }

    // ─────────────────────────────────────
    // ✅ Bonus type chips
    // ─────────────────────────────────────
    private fun setupBonusTypes() {
        binding.chipGroupBonus.removeAllViews()
        bonusTypes.forEach { type ->
            val chip = Chip(this).apply {
                text = type
                isCheckable = true
                isChecked = type == viewModel.selectedBonusType
                setChipBackgroundColorResource(
                    if (type == viewModel.selectedBonusType) R.color.purple_light
                    else R.color.white
                )
                setOnClickListener {
                    viewModel.selectedBonusType = type
                    setupBonusTypes()
                }
            }
            binding.chipGroupBonus.addView(chip)
        }
    }

    // ─────────────────────────────────────
    // ✅ Shift picker bottom sheet
    // ─────────────────────────────────────
    private fun showShiftPicker() {
        if (shifts.isEmpty()) {
            Snackbar.make(binding.root, "No shifts found", Snackbar.LENGTH_SHORT).show()
            return
        }

        val dialogBinding = DialogShiftPickerBinding
            .inflate(LayoutInflater.from(this))
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogBinding.root)

        val adapter = com.blivtech.emptrack.ui.entry.adapter.ShiftPickerAdapter { shift ->
            viewModel.selectedShiftCode = shift.shiftCode
            viewModel.selectedShiftName = shift.shiftName
            binding.tvShift.text = "${shift.shiftName}"
            binding.tvShiftTime.text =
                "${shift.startTime.toString().take(5)} – ${shift.endTime.toString().take(5)}"
            dialog.dismiss()
        }
        adapter.submitList(shifts)
        dialogBinding.rvShifts.layoutManager = LinearLayoutManager(this)
        dialogBinding.rvShifts.adapter = adapter

        dialog.show()
    }

    // ─────────────────────────────────────
    // ✅ Repay month picker
    // ─────────────────────────────────────
    private fun showRepayMonthPicker() {
        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }
        DatePickerDialog(
            this,
            { _, year, month, _ ->
                cal.set(year, month, 1)
                viewModel.selectedRepayMonth = monthFmt.format(cal.time)
                binding.tvRepayMonth.text = monthDisplayFmt.format(cal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            1
        ).show()
    }

    // ─────────────────────────────────────
    // ✅ Employee picker bottom sheet
    // ─────────────────────────────────────
    private fun showEmployeePicker() {
        val allEmps = viewModel.employees.value ?: return

        val dialogBinding = DialogEmployeePickerBinding
            .inflate(LayoutInflater.from(this))
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogBinding.root)

        val isSingle = viewModel.entryMode == EntryMode.SINGLE

        dialogBinding.tvPickerTitle.text =
            if (isSingle) "Select employee" else "Select employees"
        dialogBinding.tvPickerSub.text =
            if (isSingle) "Tap to select — auto closes"
            else "Tap checkboxes → tap Done"
        dialogBinding.btnDone.visibility =
            if (isSingle) View.GONE else View.VISIBLE

        var filteredEmps = allEmps.toMutableList()

        val pickerAdapter = EmployeePickerAdapter(
            isSingle = isSingle,
            selectedCodes = viewModel.selectedEmployees.map { it.empCode }.toMutableSet(),
            onSelect = { emp ->
                viewModel.selectEmployee(emp)
                if (isSingle) {
                    refreshEmployeePills()
                    updateSaveButton()
                    dialog.dismiss()
                }
            }
        )
        pickerAdapter.submitList(filteredEmps)
        dialogBinding.rvEmployees.layoutManager = LinearLayoutManager(this)
        dialogBinding.rvEmployees.adapter = pickerAdapter

        // ✅ Search
        dialogBinding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s?.toString()?.trim()?.lowercase() ?: ""
                filteredEmps = if (q.isEmpty()) allEmps.toMutableList()
                else allEmps.filter {
                    it.name.lowercase().contains(q) ||
                    it.empCode.lowercase().contains(q)
                }.toMutableList()
                pickerAdapter.submitList(filteredEmps)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // ✅ Done button for multiple
        dialogBinding.btnDone.setOnClickListener {
            refreshEmployeePills()
            updateSaveButton()
            dialog.dismiss()
        }

        dialog.show()
    }

    // ─────────────────────────────────────
    // ✅ Refresh employee display
    // ─────────────────────────────────────
    private fun refreshEmployeePills() {
        val selected = viewModel.selectedEmployees

        if (viewModel.entryMode == EntryMode.SINGLE) {
            if (selected.isNotEmpty()) {
                val emp = selected.first()
                val initials = emp.name.split(" ")
                    .take(2).joinToString("") { it.first().uppercase() }
                binding.tvEmpInitials.text = initials
                binding.tvEmpName.text = emp.name
                binding.tvEmpCode.text = emp.empCode
                binding.layoutSingleEmployee.visibility = View.VISIBLE
            }
        } else {
            // ✅ Build pills
            binding.chipGroupEmployees.removeAllViews()
            selected.forEach { emp ->
                val chip = Chip(this).apply {
                    text = emp.name.split(" ").first()
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        viewModel.removeEmployee(emp.empCode)
                        refreshEmployeePills()
                        updateSaveButton()
                    }
                }
                binding.chipGroupEmployees.addView(chip)
            }
            // ✅ Add more chip
            val addChip = Chip(this).apply {
                text = "+ Add more"
                chipBackgroundColor =
                    android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.WHITE
                    )
                setOnClickListener { showEmployeePicker() }
            }
            binding.chipGroupEmployees.addView(addChip)
        }
    }

    // ─────────────────────────────────────
    // ✅ Update save button
    // ─────────────────────────────────────
    private fun updateSaveButton() {
        val hasEmployees = viewModel.selectedEmployees.isNotEmpty()
        val hasAmount    = viewModel.amount > 0
        val isReady      = hasEmployees && hasAmount

        binding.btnSave.text = viewModel.getSaveButtonText()
        binding.btnSave.isEnabled = isReady
        binding.btnSave.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                if (isReady) viewModel.getSaveButtonColor()
                else android.graphics.Color.parseColor("#BDBDBD")
            )
    }

    // ─────────────────────────────────────
    // ✅ Observe save state
    // ─────────────────────────────────────
    private fun observeData() {
        viewModel.saveState.observe(this) { resource ->
            resource ?: return@observe
            when (resource) {
                is Resource.Loading -> {
                    binding.btnSave.isEnabled = false
                    binding.btnSave.text = "Saving..."
                }
                is Resource.Success -> {
                    Snackbar.make(binding.root, "Saved successfully! ✅",
                        Snackbar.LENGTH_SHORT).show()
                    viewModel.resetState()
                    finish()
                }
                is Resource.Error -> {
                    binding.btnSave.isEnabled = true
                    updateSaveButton()
                    Snackbar.make(binding.root, resource.message,
                        Snackbar.LENGTH_LONG).show()
                    viewModel.resetState()
                }
            }
        }
    }
}