package com.blivtech.emptrack.ui.employee.bottomsheet

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blivtech.emptrack.databinding.BottomsheetSalaryBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.NumberFormat
import java.util.Locale

class SalaryBottomSheet(
    private val selectedType: Int? = null,
    private val selectedAmount: Double? = null,
    private val onConfirmed: (type: Int, typeName: String, amount: Double) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomsheetSalaryBinding
    private var selectedSalaryType = selectedType ?: 1 // Default Daily

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomsheetSalaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Pre-fill if editing
        selectedAmount?.let {
            binding.etAmount.setText(it.toString())
        }
        updateTypeSelection(selectedSalaryType)
        updateSummary()
        setupListeners()
    }

    private fun setupListeners() {
        binding.ivClose.setOnClickListener { dismiss() }

        // ✅ Salary type selection
        binding.layoutDaily.setOnClickListener {
            selectedSalaryType = 1
            updateTypeSelection(1)
            updateSuffix("/ day")
            updateSummary()
        }
        binding.layoutWeekly.setOnClickListener {
            selectedSalaryType = 2
            updateTypeSelection(2)
            updateSuffix("/ week")
            updateSummary()
        }
        binding.layoutMonthly.setOnClickListener {
            selectedSalaryType = 3
            updateTypeSelection(3)
            updateSuffix("/ month")
            updateSummary()
        }

        // ✅ Amount input → update summary live
        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateSummary() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ✅ Confirm
        binding.btnConfirmSalary.setOnClickListener {
            val amount = binding.etAmount.text.toString().toDoubleOrNull()
            if (amount == null || amount <= 0) {
                binding.tilAmount.error = "Enter a valid amount"
                return@setOnClickListener
            }
            binding.tilAmount.error = null
            val typeName = when (selectedSalaryType) {
                1 -> "Daily"
                2 -> "Weekly"
                3 -> "Monthly"
                else -> "Daily"
            }
            onConfirmed(selectedSalaryType, typeName, amount)
            dismiss()
        }
    }

    private fun updateTypeSelection(type: Int) {
        binding.layoutDaily.setBackgroundResource(
            if (type == 1) com.blivtech.emptrack.R.drawable.bg_salary_type_selected
            else com.blivtech.emptrack.R.drawable.bg_salary_type
        )
        binding.layoutWeekly.setBackgroundResource(
            if (type == 2) com.blivtech.emptrack.R.drawable.bg_salary_type_selected
            else com.blivtech.emptrack.R.drawable.bg_salary_type
        )
        binding.layoutMonthly.setBackgroundResource(
            if (type == 3) com.blivtech.emptrack.R.drawable.bg_salary_type_selected
            else com.blivtech.emptrack.R.drawable.bg_salary_type
        )
    }

    private fun updateSuffix(suffix: String) {
        binding.tilAmount.suffixText = suffix
    }

    private fun updateSummary() {
        val amount = binding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
        val fmt = NumberFormat.getNumberInstance(Locale("en", "IN"))

        when (selectedSalaryType) {
            1 -> { // Daily
                binding.tvDailyRate.text = "₹ ${fmt.format(amount)}"
                binding.tvWeeklyRate.text = "₹ ${fmt.format(amount * 6)}"
                binding.tvMonthlyRate.text = "₹ ${fmt.format(amount * 26)}"
            }
            2 -> { // Weekly
                binding.tvDailyRate.text = "₹ ${fmt.format(amount / 6)}"
                binding.tvWeeklyRate.text = "₹ ${fmt.format(amount)}"
                binding.tvMonthlyRate.text = "₹ ${fmt.format(amount * 4)}"
            }
            3 -> { // Monthly
                binding.tvDailyRate.text = "₹ ${fmt.format(amount / 26)}"
                binding.tvWeeklyRate.text = "₹ ${fmt.format(amount / 4)}"
                binding.tvMonthlyRate.text = "₹ ${fmt.format(amount)}"
            }
        }
    }
}