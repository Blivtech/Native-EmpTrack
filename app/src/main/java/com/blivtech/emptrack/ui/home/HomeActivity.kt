package com.blivtech.emptrack.ui.home

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.blivtech.emptrack.R
import com.blivtech.emptrack.databinding.ActivityHomeBinding
import com.blivtech.emptrack.databinding.DialogSyncBinding
import com.blivtech.emptrack.ui.AboutActivity
import com.blivtech.emptrack.ui.HelpActivity
import com.blivtech.emptrack.ui.company.CompanyListActivity
import com.blivtech.emptrack.ui.login.LoginActivity
import com.blivtech.emptrack.ui.profile.ProfileActivity
import com.blivtech.emptrack.ui.profile.SettingsActivity
import com.blivtech.emptrack.utils.PreferenceManager
import com.blivtech.emptrack.utils.Resource
import com.blivtech.emptrack.utils.SyncEventBus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    // ✅ Sync dialog — kept here since it's app-wide, not per-fragment
    private lateinit var syncDialogBinding: DialogSyncBinding
    private lateinit var syncDialog: Dialog
    private val syncViewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var btCode = ""
    private val fromLogin by lazy { intent.getBooleanExtra("fromLogin", false) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavController()
        setupBottomNav()
        setupDrawerActions()

        lifecycleScope.launch {
            btCode = preferenceManager.btCode.first()
            if (fromLogin) showSyncDialog()
        }
    }

    // ─────────────────────────────────
    // ✅ NavController setup
    // ─────────────────────────────────
    private fun setupNavController() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    // ─────────────────────────────────
    // ✅ Bottom nav — manual mapping (menu ids ≠ destination ids)
    // ─────────────────────────────────
    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }
                R.id.nav_attendance -> {
                    navController.navigate(R.id.attendanceFragment)
                    true
                }
                R.id.nav_employee -> {
                    navController.navigate(R.id.employeeFragment)
                    true
                }
                R.id.nav_reports -> {
                    navController.navigate(R.id.reportsFragment)
                    true
                }
                else -> false
            }
        }
    }

    // ─────────────────────────────────
    // ✅ Drawer — opened from HomeFragment via avatar tap
    // (see openDrawer() helper below)
    // ─────────────────────────────────
    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.END)
    }

    private fun setupDrawerActions() {
        binding.layout.menuSync.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            showSyncDialog()
        }
        binding.layout.menuCompanies.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            startActivity(Intent(this, CompanyListActivity::class.java))
        }
        binding.layout.menuProfile.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.layout.menuAbout.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            startActivity(Intent(this, AboutActivity::class.java))
        }
        binding.layout.menuSettings.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.layout.menuHelp.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            startActivity(Intent(this, HelpActivity::class.java))
        }
        binding.layout.layoutLogout.setOnClickListener {
            showLogoutDialog()
        }

        // ✅ Drawer user info — same as old HomeActivity.observeData() tail
        lifecycleScope.launch {
            val name = preferenceManager.userName.first()
            val initials = name.split(" ")
                .take(2).joinToString("") { it.first().uppercase() }
            binding.layout.tvDrawerAvatar.text = initials
            binding.layout.tvDrawerName.text   = name
            binding.layout.tvDrawerPhone.text  = preferenceManager.btCode.first()
            binding.layout.tvDrawerCode.text   = "${preferenceManager.btCode.first()} · Admin"
        }
    }

    // ─────────────────────────────────
    // ✅ Logout
    // ─────────────────────────────────
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ -> logout() }
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

    // ─────────────────────────────────
    // ✅ Sync dialog — unchanged logic from old HomeActivity
    // ─────────────────────────────────
    private fun showSyncDialog() {
        syncDialogBinding = DialogSyncBinding.inflate(layoutInflater)
        syncDialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(syncDialogBinding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setDimAmount(0.6f)
            window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.88).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setCancelable(false)
        }
        syncDialog.show()
        observeSyncState()
        animateSyncSteps()
        syncViewModel.syncMasterData(btCode)
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
        syncViewModel.syncState.observe(this) { resource ->
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
                        // ✅ Tell HomeFragment to refresh — via simple broadcast LiveData
                        SyncEventBus.notifySyncComplete()
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (syncDialog.isShowing) {
                            syncDialog.dismiss()
                            SyncEventBus.notifySyncComplete()
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
                        syncViewModel.syncMasterData(btCode)
                    }
                }
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
        } else if (!navController.popBackStack()) {
            super.onBackPressed()
        }
    }
}