package com.blivtech.emptrack.ui.company

import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blivtech.emptrack.data.local.entity.CompanyEntity
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.data.model.parcel.CompanyParcel
import com.blivtech.emptrack.data.model.parcel.ShiftParcel
import com.blivtech.emptrack.data.model.CompanyRequest
import com.blivtech.emptrack.databinding.ActivityAddEditCompanyBinding
import com.blivtech.emptrack.databinding.DialogAddShiftBinding
import com.blivtech.emptrack.ui.company.adapter.ShiftAdapter
import com.blivtech.emptrack.utils.PreferenceManager
import com.blivtech.emptrack.utils.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class AddEditCompanyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditCompanyBinding
    private val viewModel: AddEditCompanyViewModel by viewModels()
    private lateinit var shiftAdapter: ShiftAdapter

    @Inject
    lateinit var preferenceManager: PreferenceManager
    private var companyCode = ""
    private var companyName = ""
    // ✅ Edit mode data
    private var editCompanyId: String = ""
    private var btCode  = ""
    private val isEditMode get() = editCompanyId != ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditCompanyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Check if edit mode
        editCompanyId = intent.getStringExtra("companyCode", )?:""


        lifecycleScope.launch {
            companyCode = preferenceManager.selectedCompanyCode.first()
            companyName = preferenceManager.selectedCompanyName.first()}

        getValuesFromDataStore()
        setupUI()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        // ✅ Pre-fill if edit mode
        if (isEditMode) prefillData()
    }

    private fun getValuesFromDataStore(){
        lifecycleScope.launch {
            btCode = preferenceManager.btCode.first()
        }
    }

    private fun setupUI() {
        binding.tvTitle.text = if (isEditMode) "Edit company" else "Add company"
        binding.btnSave.text = if (isEditMode) "Update company" else "Save company"
        binding.btnDelete.visibility = if (isEditMode) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerView() {
        shiftAdapter = ShiftAdapter { index ->
            viewModel.removeShift(index)
        }
        binding.rvShifts.apply {
            layoutManager = LinearLayoutManager(this@AddEditCompanyActivity)
            adapter = shiftAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.btnAddShift.setOnClickListener { showAddShiftDialog() }

        binding.btnSave.setOnClickListener {
            if (validateInputs()) saveCompany()
        }

        binding.btnDelete.setOnClickListener {
            // TODO: confirm delete dialog
        }
    }

    private fun observeViewModel() {
        // ✅ Observe shift list changes
        viewModel.shifts.observe(this) { shifts ->
            shiftAdapter.submitList(shifts.toList())
            binding.tvNoShifts.visibility =
                if (shifts.isEmpty()) View.VISIBLE else View.GONE
        }

        // ✅ Observe save state
        viewModel.saveState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Snackbar.make(
                        binding.root,
                        if (isEditMode) "Company updated!" else "Company created!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Snackbar.make(binding.root, resource.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun prefillData() {
        val company = intent.getParcelableExtra<CompanyParcel>("company")   // ✅ Parcel
        val shifts = intent.getParcelableArrayListExtra<ShiftParcel>("shifts") // ✅ Parcel

        company?.let { c ->
            binding.etName.setText(c.name)
            binding.etAddress.setText(c.address)
            binding.etCity.setText(c.city)
            binding.etState.setText(c.state)
            binding.etPhone.setText(c.phone)
            binding.etEmail.setText(c.email)
        }

        shifts?.let { list ->
            val shiftRequests = list.map { s ->
                CompanyRequest.ShiftRequest(
                    shiftName = s.shiftName,
                    startTime = s.startTime,
                    endTime = s.endTime,
                    shiftCode = s.shiftCode,
                )
            }
            viewModel.setShifts(shiftRequests)
        }
    }

    // ✅ Add shift dialog with TimePicker
    private fun showAddShiftDialog() {
        val dialogBinding = DialogAddShiftBinding.inflate(layoutInflater)

        val dialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(dialogBinding.root)

            // ✅ Critical lines
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setDimAmount(0.6f)
            window?.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND
            )
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.90).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // ✅ Time pickers
        dialogBinding.etStartTime.setOnClickListener {
            showTimePicker { time -> dialogBinding.etStartTime.setText(time) }
        }
        dialogBinding.tilStartTime.setEndIconOnClickListener {
            showTimePicker { time -> dialogBinding.etStartTime.setText(time) }
        }

        dialogBinding.etEndTime.setOnClickListener {
            showTimePicker { time -> dialogBinding.etEndTime.setText(time) }
        }
        dialogBinding.tilEndTime.setEndIconOnClickListener {
            showTimePicker { time -> dialogBinding.etEndTime.setText(time) }
        }

        dialogBinding.btnCancelShift.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnSaveShift.setOnClickListener {
            val name = dialogBinding.etShiftName.text.toString().trim()
            val start = dialogBinding.etStartTime.text.toString().trim()
            val end = dialogBinding.etEndTime.text.toString().trim()

            if (name.isEmpty()) {
                dialogBinding.tilShiftName.error = "Shift name required"
                return@setOnClickListener
            }
            if (start.isEmpty()) {
                dialogBinding.tilStartTime.error = "Start time required"
                return@setOnClickListener
            }
            if (end.isEmpty()) {
                dialogBinding.tilEndTime.error = "End time required"
                return@setOnClickListener
            }

            viewModel.addShift(
                CompanyRequest.ShiftRequest(
                    shiftName = name,
                    startTime = start,
                    endTime = end,
                    shiftCode = ""
                )
            )
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                onTimeSelected(String.format("%02d:%02d:00", hour, minute))
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            binding.tilName.error = "Company name is required"
            isValid = false
        } else {
            binding.tilName.error = null
        }
        if (viewModel.shifts.value.isNullOrEmpty()) {
            Snackbar.make(binding.root, "Add at least one shift!", Snackbar.LENGTH_SHORT).show()
            isValid = false
        }
        return isValid
    }

    private fun saveCompany() {
        val request = CompanyRequest(
            btCode = btCode,
            name = binding.etName.text.toString().trim(),
            address = binding.etAddress.text.toString().trim().ifEmpty { null },
            city = binding.etCity.text.toString().trim().ifEmpty { null },
            state = binding.etState.text.toString().trim().ifEmpty { null },
            phone = binding.etPhone.text.toString().trim().ifEmpty { null },
            email = binding.etEmail.text.toString().trim().ifEmpty { null },
            logo = null,
            shifts = viewModel.shifts.value ?: emptyList()
        )

        if (isEditMode) {
            viewModel.updateCompany(companyCode, request)
        } else {
            viewModel.createCompany(request)
        }
    }
}