package com.blivtech.emptrack.ui.employee

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.DepartmentEntity
import com.blivtech.emptrack.data.local.entity.DesignationEntity
import com.blivtech.emptrack.data.model.EmployeeRequest
import com.blivtech.emptrack.databinding.ActivityAddEditEmployeeBinding
import com.blivtech.emptrack.ui.employee.bottomsheet.DepartmentBottomSheet
import com.blivtech.emptrack.ui.employee.bottomsheet.DesignationBottomSheet
import com.blivtech.emptrack.ui.employee.bottomsheet.GenderBottomSheet
import com.blivtech.emptrack.ui.employee.bottomsheet.SalaryBottomSheet
import com.blivtech.emptrack.utils.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class AddEditEmployeeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditEmployeeBinding
    private val viewModel: AddEditEmployeeViewModel by viewModels()

    // ─────────────────────────────────────────
    // Intent data
    // ─────────────────────────────────────────
    private val btCode by lazy { intent.getStringExtra("btCode") ?: "" }
    private val companyId by lazy { intent.getLongExtra("companyId", -1L) }
    private val companyName by lazy { intent.getStringExtra("companyName") ?: "" }
    private val editEmployeeId by lazy { intent.getLongExtra("employeeId", -1L) }
    private val isEditMode get() = editEmployeeId != -1L

    // ─────────────────────────────────────────
    // Selected values
    // ─────────────────────────────────────────
    private var selectedDepartment: DepartmentEntity? = null
    private var selectedDesignation: DesignationEntity? = null
    private var selectedGender: Int? = null
    private var selectedSalaryType: Int? = null
    private var selectedSalaryAmount: Double? = null
    private var selectedDob: String? = null
    private var selectedJoiningDate: String? = null

    // ─────────────────────────────────────────
    // Cached lists
    // ─────────────────────────────────────────
    private var departments = listOf<DepartmentEntity>()
    private var designations = listOf<DesignationEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditEmployeeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupClickListeners()
        observeViewModel()

        if (isEditMode) prefillData()
    }

    // ─────────────────────────────────────────
    // UI Setup
    // ─────────────────────────────────────────

    private fun setupUI() {
        binding.tvTitle.text = if (isEditMode) "Edit employee" else "Add employee"
        // ✅ Company is pre-filled and locked
        binding.tvCompanyValue.text = companyName
    }

    // ─────────────────────────────────────────
    // Click Listeners
    // ─────────────────────────────────────────

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        // ✅ Gender bottom sheet
        binding.layoutGenderTrigger.setOnClickListener {
            GenderBottomSheet(
                selectedGender = selectedGender,
                onSelected = { gender, name ->
                    selectedGender = gender
                    setFieldSelected(
                        layout = binding.layoutGenderTrigger,
                        valueView = binding.tvGenderValue,
                        value = name
                    )
                }
            ).show(supportFragmentManager, "GenderSheet")
        }

        // ✅ Department bottom sheet
        binding.layoutDeptTrigger.setOnClickListener {
            DepartmentBottomSheet(
                departments = departments,
                selectedId = selectedDepartment?.id,
                onSelected = { dept ->
                    selectedDepartment = dept
                    setFieldSelected(
                        layout = binding.layoutDeptTrigger,
                        valueView = binding.tvDeptValue,
                        value = dept.name
                    )
                },
                onAddNew = { name, desc ->
                    viewModel.createDepartment(btCode, name, desc)
                }
            ).show(supportFragmentManager, "DeptSheet")
        }

        // ✅ Designation bottom sheet
        binding.layoutDesgTrigger.setOnClickListener {
            DesignationBottomSheet(
                designations = designations,
                selectedId = selectedDesignation?.id,
                onSelected = { desg ->
                    selectedDesignation = desg
                    setFieldSelected(
                        layout = binding.layoutDesgTrigger,
                        valueView = binding.tvDesgValue,
                        value = desg.name
                    )
                },
                onAddNew = { name, desc ->
                    viewModel.createDesignation(btCode, name, desc)
                }
            ).show(supportFragmentManager, "DesgSheet")
        }

        // ✅ Salary bottom sheet
        binding.layoutSalaryTrigger.setOnClickListener {
            SalaryBottomSheet(
                selectedType = selectedSalaryType,
                selectedAmount = selectedSalaryAmount,
                onConfirmed = { type, typeName, amount ->
                    selectedSalaryType = type
                    selectedSalaryAmount = amount
                    val fmt = NumberFormat.getNumberInstance(Locale("en", "IN"))
                    setFieldSelected(
                        layout = binding.layoutSalaryTrigger,
                        valueView = binding.tvSalaryValue,
                        value = "$typeName · ₹ ${fmt.format(amount)}"
                    )
                }
            ).show(supportFragmentManager, "SalarySheet")
        }

        // ✅ Date of birth picker
        binding.layoutDobTrigger.setOnClickListener {
            showDatePicker { date ->
                selectedDob = date
                setFieldSelected(
                    layout = binding.layoutDobTrigger,
                    valueView = binding.tvDobValue,
                    value = formatDateDisplay(date)
                )
            }
        }

        // ✅ Joining date picker
        binding.layoutJoiningTrigger.setOnClickListener {
            showDatePicker { date ->
                selectedJoiningDate = date
                setFieldSelected(
                    layout = binding.layoutJoiningTrigger,
                    valueView = binding.tvJoiningValue,
                    value = formatDateDisplay(date)
                )
            }
        }

        // ✅ Save button
        binding.btnSave.setOnClickListener {
            if (validateInputs()) saveEmployee()
        }
    }

    // ─────────────────────────────────────────
    // Observe ViewModel
    // ─────────────────────────────────────────

    private fun observeViewModel() {

        // ✅ Load departments from Room
        viewModel.getDepartments(btCode).observe(this) { depts ->
            departments = depts
        }

        // ✅ Load designations from Room
        viewModel.getDesignations(btCode).observe(this) { desgs ->
            designations = desgs
        }

        // ✅ Save state
        viewModel.saveState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                    binding.btnSave.text = "Saving..."
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = if (isEditMode) "Update employee" else "Save employee"
                    Snackbar.make(binding.root, "Employee saved! 🎉", Snackbar.LENGTH_SHORT).show()
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = if (isEditMode) "Update employee" else "Save employee"
                    Snackbar.make(binding.root, resource.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        // ✅ New department created → refresh list
        viewModel.deptCreated.observe(this) { dept ->
            dept?.let {
                departments = departments + it
                selectedDepartment = it
                setFieldSelected(
                    layout = binding.layoutDeptTrigger,
                    valueView = binding.tvDeptValue,
                    value = it.name
                )
                Snackbar.make(binding.root, "${it.name} added!", Snackbar.LENGTH_SHORT).show()
            }
        }

        // ✅ New designation created → refresh list
        viewModel.desgCreated.observe(this) { desg ->
            desg?.let {
                designations = designations + it
                selectedDesignation = it
                setFieldSelected(
                    layout = binding.layoutDesgTrigger,
                    valueView = binding.tvDesgValue,
                    value = it.name
                )
                Snackbar.make(binding.root, "${it.name} added!", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    // ─────────────────────────────────────────
    // Pre-fill for edit mode
    // ─────────────────────────────────────────

    private fun prefillData() {
        val emp = intent.getParcelableExtra<com.blivtech.emptrack.data.model.parcel.EmployeeParcel>("employee")
        emp?.let { e ->
            binding.etName.setText(e.name)
            binding.etPhone.setText(e.phone)
            binding.etEmail.setText(e.email)

            e.gender?.let { g ->
                selectedGender = g
                setFieldSelected(binding.layoutGenderTrigger, binding.tvGenderValue,
                    when(g) { 1 -> "Male" 2 -> "Female" else -> "Other" })
            }
            e.dob?.let { d ->
                selectedDob = d
                setFieldSelected(binding.layoutDobTrigger, binding.tvDobValue, formatDateDisplay(d))
            }
            e.joiningDate?.let { j ->
                selectedJoiningDate = j
                setFieldSelected(binding.layoutJoiningTrigger, binding.tvJoiningValue, formatDateDisplay(j))
            }
            e.salaryType?.let { t ->
                e.salaryAmount?.let { a ->
                    selectedSalaryType = t
                    selectedSalaryAmount = a
                    val typeName = when(t) { 1 -> "Daily" 2 -> "Weekly" else -> "Monthly" }
                    val fmt = NumberFormat.getNumberInstance(Locale("en", "IN"))
                    setFieldSelected(binding.layoutSalaryTrigger, binding.tvSalaryValue,
                        "$typeName · ₹ ${fmt.format(a)}")
                }
            }
        }
    }

    // ─────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────

    // ✅ Mark field as selected (blue border + value)
    private fun setFieldSelected(
        layout: View,
        valueView: android.widget.TextView,
        value: String
    ) {
        layout.setBackgroundResource(R.drawable.bg_field_selected)
        valueView.text = value
        valueView.setTextColor(getColor(android.R.color.holo_blue_dark))
    }

    // ✅ Date picker dialog
    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val date = String.format("%04d-%02d-%02d", year, month + 1, day)
                onDateSelected(date)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // ✅ Format date for display: 2023-01-15 → 15 Jan 2023
    private fun formatDateDisplay(date: String): String {
        return try {
            val input = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val output = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            output.format(input.parse(date) ?: return date)
        } catch (e: Exception) { date }
    }

    // ─────────────────────────────────────────
    // Validation
    // ─────────────────────────────────────────

    private fun validateInputs(): Boolean {
        var isValid = true

        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"
            isValid = false
        } else binding.tilName.error = null

        if (phone.isEmpty()) {
            binding.tilPhone.error = "Phone is required"
            isValid = false
        } else if (phone.length < 10) {
            binding.tilPhone.error = "Enter valid phone number"
            isValid = false
        } else binding.tilPhone.error = null

        if (selectedDepartment == null) {
            Snackbar.make(binding.root, "Please select a department", Snackbar.LENGTH_SHORT).show()
            isValid = false
        }

        if (selectedDesignation == null) {
            Snackbar.make(binding.root, "Please select a designation", Snackbar.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    // ─────────────────────────────────────────
    // Save Employee
    // ─────────────────────────────────────────

    private fun saveEmployee() {
        val empCode = if (isEditMode) "" else "EMP${System.currentTimeMillis()}"

        val request = EmployeeRequest(
            btCode        = btCode,
            empCode       = empCode,
            companyId     = companyId,
            departmentId  = selectedDepartment?.id ?: -1L,
            designationId = selectedDesignation?.id ?: -1L,
            name          = binding.etName.text.toString().trim(),
            email         = binding.etEmail.text.toString().trim().ifEmpty { null },
            phone         = binding.etPhone.text.toString().trim(),
            gender        = selectedGender,
            dob           = selectedDob,
            joiningDate   = selectedJoiningDate,
            salaryType    = selectedSalaryType,
            salaryAmount  = selectedSalaryAmount,
            status        = 1
        )

        if (isEditMode) viewModel.updateEmployee(editEmployeeId, request)
        else viewModel.createEmployee(request)
    }
}