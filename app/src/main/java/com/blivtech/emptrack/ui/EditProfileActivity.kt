package com.blivtech.emptrack.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.databinding.ActivityEditProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }
        loadCurrent()
        binding.btnSave.setOnClickListener { save() }
    }

    private fun loadCurrent() {
        // TODO: prefill from PreferenceManager / API
        binding.etFullName.setText("Sakthi")
        binding.etPhone.setText("+91 98765 43210")
        binding.etEmail.setText("sakthi@blivtech.com")
        binding.etUserType.setText("Admin")   // disabled field
        binding.tvAvatar.text = "S"
    }

    private fun save() {
        val name  = binding.etFullName.text?.toString()?.trim().orEmpty()
        val phone = binding.etPhone.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()

        if (name.isEmpty()) { binding.tilFullName.error = "Enter your name"; return } else binding.tilFullName.error = null
        if (phone.length < 8) { binding.tilPhone.error = "Enter a valid phone"; return } else binding.tilPhone.error = null

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        // TODO: call your update-profile API here, then on success:
        // (simulated success)
        binding.progressBar.visibility = View.GONE
        binding.btnSave.isEnabled = true
        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }
}
