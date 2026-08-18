package com.blivtech.emptrack.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.databinding.ActivityProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProfile()
        setupNav()
    }

    // ─────────────────────────────────────────
    // Load profile (TODO: from PreferenceManager / API)
    // ─────────────────────────────────────────
    private fun loadProfile() {
        // TODO: replace with your real values
        val name     = "Sakthi"
        val role     = "Admin"
        val phone    = "+91 98765 43210"
        val email    = "sakthi@blivtech.com"

        binding.tvName.text  = name
        binding.tvRole.text  = role
        binding.tvPhone.text = phone
        binding.tvAvatar.text = name.trim().firstOrNull()?.uppercase() ?: "U"

        binding.tvInfoName.text     = name
        binding.tvInfoPhone.text    = phone
        binding.tvInfoEmail.text    = email
        binding.tvInfoUserType.text = role
    }

    // ─────────────────────────────────────────
    // Navigation
    // ─────────────────────────────────────────
    private fun setupNav() {
        binding.ivBack.setOnClickListener { finish() }

        val openEdit = {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        binding.ivEdit.setOnClickListener { openEdit() }
        binding.btnEditProfile.setOnClickListener { openEdit() }

        binding.btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        binding.btnLogout.setOnClickListener { confirmLogout() }
    }

    // ─────────────────────────────────────────
    // Logout -> confirm -> clear session -> Login
    // ─────────────────────────────────────────
    private fun confirmLogout() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Log out?")
            .setMessage("You'll need to sign in again to use EmpTrack.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Log out") { _, _ -> doLogout() }
            .show()
    }

    private fun doLogout() {
        // TODO: clear your session, e.g. lifecycleScope.launch { preferenceManager.clearSession() }

        // Go to Login and clear the back stack.
        // Replace with your LoginActivity if you prefer an explicit target:
        //   val i = Intent(this, com.blivtech.emptrack.ui.login.LoginActivity::class.java)
        val i = packageManager.getLaunchIntentForPackage(packageName)
        i?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(i)
        finish()
    }
}
