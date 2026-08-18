package com.blivtech.emptrack.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.databinding.ActivityChangePasswordBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }
        binding.btnUpdate.setOnClickListener { update() }
    }

    private fun update() {
        val current = binding.etCurrentPassword.text?.toString().orEmpty()
        val newPass = binding.etNewPassword.text?.toString().orEmpty()
        val confirm = binding.etConfirmPassword.text?.toString().orEmpty()

        binding.tilCurrent.error = null
        binding.tilNew.error = null
        binding.tilConfirm.error = null

        if (current.isEmpty()) { binding.tilCurrent.error = "Enter current password"; return }
        if (newPass.length < 6) { binding.tilNew.error = "At least 6 characters"; return }
        if (newPass == current) { binding.tilNew.error = "Choose a different password"; return }
        if (confirm != newPass) { binding.tilConfirm.error = "Passwords don't match"; return }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnUpdate.isEnabled = false

        // TODO: call your change-password API (current, newPass), then on success:
        binding.progressBar.visibility = View.GONE
        binding.btnUpdate.isEnabled = true
        Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show()
        finish()
    }
}
