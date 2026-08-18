package com.blivtech.emptrack.ui.employee

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.DepartmentEntity
import com.blivtech.emptrack.data.local.entity.DesignationEntity
import com.blivtech.emptrack.data.model.EmployeeRequest
import com.blivtech.emptrack.databinding.ActivityAddEditEmployeeBinding
import com.blivtech.emptrack.ui.employee.bottomsheet.DepartmentBottomSheet
import com.blivtech.emptrack.ui.employee.bottomsheet.DesignationBottomSheet
import com.blivtech.emptrack.ui.employee.bottomsheet.GenderBottomSheet
import com.blivtech.emptrack.ui.employee.bottomsheet.SalaryBottomSheet
import com.blivtech.emptrack.utils.PreferenceManager
import com.blivtech.emptrack.utils.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AddEditEmployeeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditEmployeeBinding
    private val viewModel: AddEditEmployeeViewModel by viewModels()


    private var btCode  = ""
    private var companyCode= ""
    private var companyName = ""
    private val editEmployeeCode by lazy { intent.getStringExtra("employeeCode")?:"" }
    private val isEditMode get() = editEmployeeCode != ""

    @Inject
    lateinit var preferenceManager: PreferenceManager


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

        lifecycleScope.launch {
            btCode = preferenceManager.btCode.first()
            companyCode = preferenceManager.selectedCompanyCode.first()
            companyName = preferenceManager.selectedCompanyName.first()

            binding.tvCompanyName.text = companyName

            observeViewModel()

            if (isEditMode) prefillData()
        }
    }

    private fun setupUI() {
        binding.tvHeaderTitle.text = if (isEditMode) "Edit employee" else "Add employee"
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        // ✅ Gender bottom sheet
        binding.actvGender.setOnClickListener {
            GenderBottomSheet(
                selectedGender = selectedGender,
                onSelected = { gender, name ->
                    selectedGender = gender
                    setFieldSelected(
                        layout = binding.tilGender,
                        valueView = binding.actvGender,
                        value = name
                    )
                }
            ).show(supportFragmentManager, "GenderSheet")
        }

        // ✅ Department bottom sheet
        binding.actvDepartment.setOnClickListener {
            DepartmentBottomSheet(
                departments = departments,
                selectedId = selectedDepartment?.id,
                onSelected = { dept ->
                    selectedDepartment = dept
                    setFieldSelected(
                        layout = binding.tilDepartment,
                        valueView = binding.actvDepartment,
                        value = dept.name
                    )
                },
                onAddNew = { name, desc ->
                    viewModel.createDepartment(btCode, name, desc)
                }
            ).show(supportFragmentManager, "DeptSheet")
        }

        // ✅ Designation bottom sheet
        binding.actvDesignation.setOnClickListener {
            DesignationBottomSheet(
                designations = designations,
                selectedId = selectedDesignation?.id,
                onSelected = { desg ->
                    selectedDesignation = desg
                    setFieldSelected(
                        layout = binding.tilJoining,
                        valueView = binding.actvDesignation,
                        value = desg.name
                    )
                },
                onAddNew = { name, desc ->
                    viewModel.createDesignation(btCode, name, desc)
                }
            ).show(supportFragmentManager, "DesgSheet")
        }

        // ✅ Salary bottom sheet
        binding.etSalary.setOnClickListener {
            SalaryBottomSheet(
                selectedType = selectedSalaryType,
                selectedAmount = selectedSalaryAmount,
                onConfirmed = { type, typeName, amount ->
                    selectedSalaryType = type
                    selectedSalaryAmount = amount
                    val fmt = NumberFormat.getNumberInstance(Locale("en", "IN"))
                    setFieldSelected(
                        layout = binding.tilSalary,
                        valueView = binding.etSalary,
                        value = "$typeName · ₹ ${fmt.format(amount)}"
                    )
                }
            ).show(supportFragmentManager, "SalarySheet")
        }

        // ✅ Date of birth picker
        binding.etDob.setOnClickListener {
            showDatePicker { date ->
                selectedDob = date
                setFieldSelected(
                    layout = binding.tilDob,
                    valueView = binding.etDob,
                    value = formatDateDisplay(date)
                )
            }
        }

        // ✅ Joining date picker
        binding.etJoiningDate.setOnClickListener {
            showDatePicker { date ->
                selectedJoiningDate = date
                setFieldSelected(
                    layout = binding.tilJoining,
                    valueView = binding.etJoiningDate,
                    value = formatDateDisplay(date)
                )
            }
        }

        // ✅ Save button
        binding.btnSaveEmployee.setOnClickListener {
            if (validateInputs()) saveEmployee()
        }
    }


    private fun observeViewModel() {

        // ✅ Load departments from Room
        viewModel.getDepartments(btCode).observe(this) { depts ->
            departments = depts

            if (isEditMode && selectedDepartment == null) {
                val emp = intent.getParcelableExtra<com.blivtech.emptrack.data.model.parcel.EmployeeParcel>("employee")

                emp?.let {
                    selectedDepartment = departments.firstOrNull { dept ->
                        dept.deptCode == it.deptCode
                    }

                    selectedDepartment?.let { dept ->
                        setFieldSelected(
                            layout = binding.tilDepartment,
                            valueView = binding.actvDepartment,
                            value = dept.name
                        )
                    }
                }
            }
        }

        // ✅ Load designations from Room
        viewModel.getDesignations(btCode).observe(this) { desgs ->
            designations = desgs

            if (isEditMode && selectedDesignation == null) {
                val emp = intent.getParcelableExtra<com.blivtech.emptrack.data.model.parcel.EmployeeParcel>("employee")

                emp?.let {
                    selectedDesignation = designations.firstOrNull { desg ->
                        desg.desgCode == it.desgCode
                    }

                    selectedDesignation?.let { desg ->
                        setFieldSelected(
                            layout = binding.tilDesignation,
                            valueView = binding.actvDesignation,
                            value = desg.name
                        )
                    }
                }
            }
        }

        // ✅ Save state
        viewModel.saveState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSaveEmployee.isEnabled = false
                    binding.btnSaveEmployee.text = "Saving..."
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSaveEmployee.isEnabled = true
                    binding.btnSaveEmployee.text = if (isEditMode) "Update employee" else "Save employee"
                    Snackbar.make(binding.root, "Employee saved! 🎉", Snackbar.LENGTH_SHORT).show()
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSaveEmployee.isEnabled = true
                    binding.btnSaveEmployee.text = if (isEditMode) "Update employee" else "Save employee"
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
                    layout = binding.tilDepartment,
                    valueView = binding.actvDepartment,
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
                    layout = binding.tilDesignation,
                    valueView = binding.actvDesignation,
                    value = it.name
                )
                Snackbar.make(binding.root, "${it.name} added!", Snackbar.LENGTH_SHORT).show()
            }
        }
    }


    private fun prefillData() {
        val emp = intent.getParcelableExtra<com.blivtech.emptrack.data.model.parcel.EmployeeParcel>("employee")
        emp?.let { e ->
            binding.etFullName.setText(e.name)
            binding.etPhone.setText(e.phone)
            binding.etEmail.setText(e.email)

            e.gender?.let { g ->
                selectedGender = g
                setFieldSelected(binding.tilGender, binding.actvGender,
                    when(g) { 1 -> "Male" 2 -> "Female" else -> "Other" })
            }
            e.dob?.let { d ->
                selectedDob = d
                setFieldSelected(binding.tilDob, binding.etDob, formatDateDisplay(d))
            }
            e.joiningDate?.let { j ->
                selectedJoiningDate = j
                setFieldSelected(binding.tilJoining, binding.etJoiningDate, formatDateDisplay(j))
            }
            e.salaryType?.let { t ->
                e.salaryAmount?.let { a ->
                    selectedSalaryType = t
                    selectedSalaryAmount = a
                    val typeName = when(t) { 1 -> "Daily" 2 -> "Weekly" else -> "Monthly" }
                    val fmt = NumberFormat.getNumberInstance(Locale("en", "IN"))
                    setFieldSelected(binding.tilSalary, binding.etSalary,
                        "$typeName · ₹ ${fmt.format(a)}")
                }
            }
        }
    }


    private fun setFieldSelected(
        layout: View,
        valueView: android.widget.TextView,
        value: String
    ) {
//        layout.setBackgroundResource(R.drawable.bg_field_selected)
        valueView.text = value
//        valueView.setTextColor(getColor(android.R.color.holo_blue_dark))
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

        val name = binding.etFullName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        if (name.isEmpty()) {
            Snackbar.make(binding.root, "Name is required", Snackbar.LENGTH_SHORT).show()
            isValid = false
        }

        if (phone.isEmpty()) {
            Snackbar.make(binding.root, "Phone is required", Snackbar.LENGTH_SHORT).show()
            isValid = false
        } else if (phone.length < 10) {
            Snackbar.make(binding.root, "Enter valid phone number", Snackbar.LENGTH_SHORT).show()
            isValid = false
        }

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
        val empCode = if (isEditMode) editEmployeeCode else ""

        val request = EmployeeRequest(
            btCode = btCode,
            empCode = empCode,
            companyCode = companyCode,
            deptCode = selectedDepartment?.deptCode ?: "",
            desgCode = selectedDesignation?.desgCode ?: "",
            name = binding.etFullName.text.toString().trim(),
            email = binding.etEmail.text.toString().trim().ifEmpty { null },
            phone = binding.etPhone.text.toString().trim(),
            gender = selectedGender,
            dob = selectedDob,
            joiningDate = selectedJoiningDate,
            salaryType = selectedSalaryType,
            salaryAmount = selectedSalaryAmount,
            status = 1

        )
        Log.d("CHECK", "deptCode = ${selectedDepartment?.deptCode}")
        Log.d("CHECK", "desgCode = ${selectedDesignation?.desgCode}")
        Log.d("CHECK", "btCode = $btCode")
        Log.d("CHECK", "companyCode = $companyCode")

        if (isEditMode) viewModel.updateEmployee(editEmployeeCode, request)
        else viewModel.createEmployee(request)
    }
}