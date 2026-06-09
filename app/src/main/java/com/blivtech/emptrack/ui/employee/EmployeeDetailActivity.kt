package com.blivtech.emptrack.ui.employee

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.databinding.ActivityEmployeeDetailBinding
import com.blivtech.emptrack.utils.EntityExtensions.toParcel
import com.blivtech.emptrack.utils.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmployeeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmployeeDetailBinding
    private val viewModel: EmployeeListViewModel by viewModels()

    private val employeeId by lazy { intent.getLongExtra("employeeId", -1L) }
    private val btCode by lazy { intent.getStringExtra("btCode") ?: "" }
    private var employee: EmployeeEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeViewModel()

        // ✅ Load employee via ViewModel
        viewModel.loadEmployeeById(employeeId)
    }

    private fun observeViewModel() {
        // ✅ Observe single employee LiveData
        viewModel.employee.observe(this) { emp ->
            emp?.let {
                employee = it
                bindData(it)
            }
        }

        // ✅ Observe delete state
        viewModel.deleteState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    Snackbar.make(binding.root, "Employee deleted!", Snackbar.LENGTH_SHORT).show()
                    finish()
                }
                is Resource.Error -> {
                    Snackbar.make(binding.root, resource.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun bindData(emp: EmployeeEntity) {
        // ✅ Avatar initials
        val initials = emp.name.split(" ")
            .take(2).joinToString("") { it.first().uppercase() }
        binding.tvAvatar.text = initials
        binding.tvEmpName.text = emp.name
        binding.tvEmpRole.text = "Company ID: ${emp.companyCode}"
        binding.tvEmpCode.text = emp.empCode

        // ✅ Status
        binding.tvStatus.text = if (emp.status == 1) "Active" else "Inactive"

        // ✅ Contact
        binding.tvPhone.text = emp.phone ?: "Not set"
        binding.tvEmail.text = emp.email ?: "Not set"

        // ✅ Personal
        binding.tvGender.text = when (emp.gender) {
            1 -> "Male"
            2 -> "Female"
            3 -> "Other"
            else -> "Not set"
        }
        binding.tvDob.text = emp.dob ?: "Not set"
        binding.tvJoiningDate.text = emp.joiningDate ?: "Not set"

        // ✅ Job
        binding.tvDepartment.text = "Dept ID: ${emp.deptCode}"
        binding.tvDesignation.text = "Desg ID: ${emp.desgCode}"
        binding.tvSalaryType.text = when (emp.salaryType) {
            1 -> "Daily"
            2 -> "Weekly"
            3 -> "Monthly"
            else -> "Not set"
        }
        binding.tvSalaryAmount.text = emp.salaryAmount?.let {
            "₹ ${String.format("%.2f", it)}"
        } ?: "Not set"
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        // ✅ Call
        binding.layoutCall.setOnClickListener {
            employee?.phone?.let { phone ->
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
            } ?: Snackbar.make(binding.root, "No phone number!", Snackbar.LENGTH_SHORT).show()
        }

        // ✅ Message
        binding.layoutMessage.setOnClickListener {
            employee?.phone?.let { phone ->
                startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")))
            }
        }

        // ✅ WhatsApp
        binding.layoutWhatsapp.setOnClickListener {
            employee?.phone?.let { phone ->
                try {
                    startActivity(
                        Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://wa.me/$phone")
                            setPackage("com.whatsapp")
                        }
                    )
                } catch (e: Exception) {
                    Snackbar.make(binding.root, "WhatsApp not installed", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnEdit.setOnClickListener {
            employee?.let { emp ->
                startActivity(
                    Intent(this, AddEditEmployeeActivity::class.java).apply {
                        putExtra("btCode", btCode)
                        putExtra("employeeId", emp.id)
                        putExtra("companyId", emp.companyCode)
                        putExtra("companyName", "")
                        putExtra("employee", emp.toParcel())  // ✅ Pass parcel
                    }
                )
            }
        }

        // ✅ Delete
        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete employee")
                .setMessage("Are you sure you want to delete ${employee?.name}?")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteEmployee(employeeId)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}