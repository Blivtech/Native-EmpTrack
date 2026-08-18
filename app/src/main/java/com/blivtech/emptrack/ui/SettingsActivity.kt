package com.blivtech.emptrack.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.databinding.ActivitySettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }
        setupNav()
        setupToggles()
    }

    private fun setupNav() {
        binding.rowEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        binding.rowChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }
        binding.rowCompanies.setOnClickListener {
            // TODO: startActivity(Intent(this, CompaniesActivity::class.java))
        }
        binding.rowLanguage.setOnClickListener {
            // TODO: show language picker (bottom sheet / dialog), then binding.tvLanguage.text = chosen
        }
        binding.rowHelp.setOnClickListener {
            // TODO: startActivity(Intent(this, HelpActivity::class.java))
        }
        binding.rowAbout.setOnClickListener {
            // TODO: startActivity(Intent(this, AboutActivity::class.java))
        }
        binding.rowRate.setOnClickListener { openPlayStore() }

        binding.btnLogout.setOnClickListener { confirmLogout() }
    }

    private fun setupToggles() {
        // TODO: load saved values from PreferenceManager and persist on change
        binding.switchNotifications.setOnCheckedChangeListener { _, isOn -> /* TODO save */ }
        binding.switchWifiSync.setOnCheckedChangeListener { _, isOn -> /* TODO save */ }
        binding.switchDarkMode.setOnCheckedChangeListener { _, isOn ->
            // TODO save; apply theme e.g. AppCompatDelegate.setDefaultNightMode(...)
        }
    }

    private fun openPlayStore() {
        val pkg = packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")))
        } catch (e: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$pkg")))
        }
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Log out?")
            .setMessage("You'll need to sign in again to use EmpTrack.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Log out") { _, _ -> doLogout() }
            .show()
    }

    private fun doLogout() {
        // TODO: clear session, e.g. lifecycleScope.launch { preferenceManager.clearSession() }
        val i = packageManager.getLaunchIntentForPackage(packageName)
        i?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(i)
        finish()
    }
}
