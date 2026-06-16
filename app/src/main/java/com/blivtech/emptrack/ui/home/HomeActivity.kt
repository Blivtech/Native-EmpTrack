package com.blivtech.emptrack.ui.home

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.CompanyEntity
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.databinding.ActivityHomeBinding
import com.blivtech.emptrack.databinding.DialogSyncBinding
import com.blivtech.emptrack.ui.attendance.AttendanceHomeActivity
import com.blivtech.emptrack.ui.company.CompanyListActivity
import com.blivtech.emptrack.ui.employee.EmployeeListActivity
import com.blivtech.emptrack.ui.entry.AddEntryActivity
import com.blivtech.emptrack.ui.login.LoginActivity
import com.blivtech.emptrack.ui.report.DailyReportActivity
import com.blivtech.emptrack.ui.shiftplan.ShiftPlanActivity
import com.blivtech.emptrack.ui.work.AddProductActivity
import com.blivtech.emptrack.ui.work.AddWorkEntryActivity
import com.blivtech.emptrack.ui.work.SelectEmployeeActivity
import com.blivtech.emptrack.utils.PreferenceManager
import com.blivtech.emptrack.utils.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var syncDialogBinding: DialogSyncBinding
    private lateinit var syncDialog: Dialog

    private lateinit var adapter: ModuleCardAdapter
    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var btCode = ""
    private val fromLogin by lazy { intent.getBooleanExtra("fromLogin", false) }
    private var currentCompany: CompanyEntity? = null
    private var currentShifts = listOf<ShiftEntity>()


    private var selectedCompanyCode = ""
    private var selectedCompanyName = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getValuesFromDataStore()
        setGreeting()
        setTodayDate()
        setupBottomNav()
        setupModules()
        setupDrawer()
        observeData()

        // ✅ Only sync on first login
        if (fromLogin) showSyncDialog()
    }

    // ─────────────────────────────────────────
    // Greeting & Date
    // ─────────────────────────────────────────

    private fun  getValuesFromDataStore() {
        lifecycleScope.launch {
            btCode = preferenceManager.btCode.first()
        }
    }

    private fun setGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hour < 12 -> "Good morning,"
            hour < 17 -> "Good afternoon,"
            else      -> "Good evening,"
        }
    }

    private fun setTodayDate() {
        val date = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(Date())
        binding.tvTodayDate.text = date
    }

    // ─────────────────────────────────────────
    // Drawer Setup
    // ─────────────────────────────────────────

    private fun setupDrawer() {

        // ✅ Open drawer on avatar tap
        binding.tvAvatar.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }

        // ✅ Sync from drawer
        binding.menuSync.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            showSyncDialog()
        }

        // ✅ Sync button in header
        binding.ivSync.setOnClickListener {
            showSyncDialog()
        }

        // ✅ Companies from drawer
        binding.menuCompanies.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
           startActivity(Intent(this, CompanyListActivity::class.java))
        }

        // ✅ Profile from drawer
        binding.menuProfile.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            // TODO: ProfileActivity
        }

        // ✅ Settings from drawer
        binding.menuSettings.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            // TODO: SettingsActivity
        }

        // ✅ Help from drawer
        binding.menuHelp.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            // TODO: HelpActivity
        }

        // ✅ Logout
        binding.layoutLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    // ─────────────────────────────────────────
    // Logout
    // ─────────────────────────────────────────

    private fun showLogoutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logout() {
        lifecycleScope.launch {
            preferenceManager.clearAll()
            startActivity(
                Intent(this@HomeActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            finish()
        }
    }

    // ─────────────────────────────────────────
    // Modules Grid
    // ─────────────────────────────────────────

    private fun setupModules() {
        adapter = ModuleCardAdapter(viewModel.moduleCards) { card ->
            navigateToModule(card.cardName)
        }

        binding.rvModules.apply {
            layoutManager = GridLayoutManager(this@HomeActivity, 2)
            this.adapter = this@HomeActivity.adapter
        }
    }

    private fun navigateToModule(name: String) {
        when (name) {
            "Employee" -> startActivity(
                Intent(this, EmployeeListActivity::class.java))

            "Attendance" -> {
                lifecycleScope.launch {
                    val companyCode = preferenceManager.selectedCompanyCode.first()
                    val companyName = preferenceManager.selectedCompanyName.first()
                    val btCode = preferenceManager.btCode.first()

                    startActivity(Intent(this@HomeActivity, AttendanceHomeActivity::class.java).apply {
                        putExtra("btCode", btCode)
                        putExtra("companyName", companyName)
                        putExtra("companyCode", companyCode)
                    })
                }
            }


            "Work Progress" -> { startActivity(
                Intent(this, AddWorkEntryActivity::class.java).apply {
                    putExtra("companyCode", currentCompany?.companyCode ?: "")
                })}
            "Salary" -> { /* TODO */ }
            "Advance" -> {// From any module card
                startActivity(
                    Intent(this, AddEntryActivity::class.java).apply {
                        putExtra("btCode", btCode)
                        putExtra("companyName", currentCompany?.name ?: "")
                        putExtra("companyCode", currentCompany?.companyCode ?: "")
                    }
                ) }
            "Inventory" -> { /* TODO */ }
            "Shift Mgmt" -> { startActivity(
                Intent(this, ShiftPlanActivity::class.java)
            )}
            "Reports" -> {  startActivity(
                Intent(this, DailyReportActivity::class.java)) }
        }
    }

    // ─────────────────────────────────────────
    // Observe Data
    // ─────────────────────────────────────────

    private fun observeData() {
        lifecycleScope.launch {
            val savedCompanyCode = preferenceManager.selectedCompanyCode.first()

            viewModel.getCompanies().observe(this@HomeActivity) { companies ->
                if (companies.isNotEmpty()) {
                    val company = companies.find { it.companyCode == savedCompanyCode } ?: companies.first()

                    currentCompany = company
                    binding.tvCompanyName.text = "${company.name} · ${company.city ?: ""}"
                    binding.tvTotalEmp.text = "50"

                    viewModel.getShifts(company.companyCode).observe(this@HomeActivity) { shifts ->
                        currentShifts = shifts
                        updateShiftCards(shifts)
                    }
                } else {
                    binding.tvCompanyName.text = "No company — tap to add"
                }
            }

            val name = preferenceManager.userName.first()
            val initials = name.split(" ").take(2).joinToString("") { it.first().uppercase() }
            binding.tvUserName.text = name
            binding.tvAvatar.text = initials
            binding.tvDrawerAvatar.text = initials
            binding.tvDrawerName.text = name
            binding.tvDrawerPhone.text = preferenceManager.btCode.first()
            binding.tvDrawerCode.text = "${preferenceManager.btCode.first()} · Admin"
        }
    }

    private fun updateShiftCards(shifts: List<ShiftEntity>) {
        binding.layoutShifts.removeAllViews()
        if (shifts.isEmpty()) {
            binding.tvActiveShift.text = "No shifts"
            return
        }
        shifts.forEachIndexed { index, shift ->
            val shiftView = LayoutInflater.from(this)
                .inflate(R.layout.item_shift_row, binding.layoutShifts, false)
            shiftView.findViewById<TextView>(R.id.tvShiftRowName).text =
                "Shift ${index + 1} · ${shift.shiftName} · ${shift.startTime.take(5)}–${shift.endTime.take(5)}"
            binding.layoutShifts.addView(shiftView)
        }
        binding.tvActiveShift.text = "Shift 1 Active"
    }

    // ─────────────────────────────────────────
    // Bottom Nav
    // ─────────────────────────────────────────

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home       -> true
                R.id.nav_attendance -> { navigateToModule("Attendance"); true }
                R.id.nav_reports    -> { navigateToModule("Reports"); true }
                R.id.nav_employee    -> { navigateToModule("Employee");true}
                else -> false
            }
        }
    }

    // ─────────────────────────────────────────
    // Sync Dialog
    // ─────────────────────────────────────────

    private fun showSyncDialog() {
        syncDialogBinding = DialogSyncBinding.inflate(layoutInflater)
        syncDialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(syncDialogBinding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setDimAmount(0.6f)
            window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.88).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setCancelable(false)
        }
        syncDialog.show()
        observeSyncState()
        animateSyncSteps()
        viewModel.syncMasterData(btCode)
    }

    private fun animateSyncSteps() {
        val handler = Handler(Looper.getMainLooper())
        updateStep(syncDialogBinding.tvStep1Icon, syncDialogBinding.tvStep1,
            "→", "#1565C0", "Fetching companies...", "#1565C0")
        syncDialogBinding.progressBar.progress = 10
        handler.postDelayed({
            updateStep(syncDialogBinding.tvStep1Icon, syncDialogBinding.tvStep1,
                "✓", "#27500A", "Companies fetched", "#27500A")
            updateStep(syncDialogBinding.tvStep2Icon, syncDialogBinding.tvStep2,
                "→", "#1565C0", "Fetching shifts...", "#1565C0")
            syncDialogBinding.progressBar.progress = 40
        }, 600)
        handler.postDelayed({
            updateStep(syncDialogBinding.tvStep2Icon, syncDialogBinding.tvStep2,
                "✓", "#27500A", "Shifts fetched", "#27500A")
            updateStep(syncDialogBinding.tvStep3Icon, syncDialogBinding.tvStep3,
                "→", "#1565C0", "Fetching departments...", "#1565C0")
            syncDialogBinding.progressBar.progress = 70
        }, 1200)
    }

    private fun observeSyncState() {
        viewModel.syncState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    syncDialogBinding.btnSyncAction.isEnabled = false
                    syncDialogBinding.btnSyncAction.text = "Syncing... please wait"
                }
                is Resource.Success -> {
                    val result = resource.data
                    syncDialogBinding.progressBar.progress = 100
                    updateStep(syncDialogBinding.tvStep3Icon, syncDialogBinding.tvStep3,
                        "✓", "#27500A", "Departments fetched", "#27500A")
                    syncDialogBinding.tvSyncTitle.text = "Sync complete!"
                    syncDialogBinding.tvSyncSubtitle.text =
                        "${result.companiesCount} companies · ${result.shiftsCount} shifts · ${result.departmentsCount} departments"
                    syncDialogBinding.ivSyncIcon.setColorFilter(Color.parseColor("#27500A"))
                    syncDialogBinding.btnSyncAction.isEnabled = true
                    syncDialogBinding.btnSyncAction.text = "Continue to dashboard"
                    syncDialogBinding.btnSyncAction.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#1565C0"))
                    syncDialogBinding.btnSyncAction.setOnClickListener {
                        syncDialog.dismiss()
                        observeData()
                        // ✅ Update last synced time
                        binding.tvLastSynced.text = "Last synced: just now"
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (syncDialog.isShowing) {
                            syncDialog.dismiss()
                            observeData()
                            binding.tvLastSynced.text = "Last synced: just now"
                        }
                    }, 2000)
                }
                is Resource.Error -> {
                    syncDialogBinding.progressBar.progress = 0
                    syncDialogBinding.tvSyncTitle.text = "Sync failed"
                    syncDialogBinding.tvSyncSubtitle.text = resource.message
                    syncDialogBinding.ivSyncIcon.setColorFilter(Color.parseColor("#791F1F"))
                    syncDialogBinding.btnSyncAction.isEnabled = true
                    syncDialogBinding.btnSyncAction.text = "Retry sync"
                    syncDialogBinding.btnSyncAction.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#E24B4A"))
                    syncDialogBinding.btnSyncAction.setOnClickListener {
                        animateSyncSteps()
                        viewModel.syncMasterData(btCode)
                    }
                }

                is Resource.Error -> TODO()
                Resource.Loading -> TODO()
                is Resource.Success -> TODO()
            }
        }
    }

    private fun updateStep(
        iconView: TextView, textView: TextView,
        icon: String, iconColor: String,
        text: String, textColor: String
    ) {
        iconView.text = icon
        iconView.setTextColor(Color.parseColor(iconColor))
        textView.text = text
        textView.setTextColor(Color.parseColor(textColor))
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            selectedCompanyCode = preferenceManager.selectedCompanyCode.first()
            selectedCompanyName = preferenceManager.selectedCompanyName.first()
            binding.tvCompanyName.text = selectedCompanyName
        }

        // ✅ Update currentCompany immediately (synchronously) — single source of truth
        val companies = viewModel.getCompanies().value
        currentCompany = companies?.find { it.companyCode == selectedCompanyCode }
            ?: currentCompany?.copy(companyCode = selectedCompanyCode, name = selectedCompanyName)

        viewModel.getShifts(selectedCompanyCode).observe(this) { shifts ->
            currentShifts = shifts
            updateShiftCards(shifts)
        }

       // Snackbar.make(binding.root, "Switched to $selectedCompanyName", Snackbar.LENGTH_SHORT).show()

    }
}