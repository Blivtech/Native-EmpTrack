package com.blivtech.emptrack.ui.signup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.data.model.RegisterRequest
import com.blivtech.emptrack.databinding.ActivitySignupBinding
import com.blivtech.emptrack.ui.login.LoginActivity
import com.blivtech.emptrack.utils.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val viewModel: SignUpViewModel by viewModels()
    private var selectedUserType: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUserTypeDropdown()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupUserTypeDropdown() {
        val userTypes = listOf("Contractor", "Manager", "Admin")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            userTypes
        )
        binding.actvUserType.setAdapter(adapter)
        binding.actvUserType.setOnItemClickListener { _, _, position, _ ->
            selectedUserType = position + 1
        }
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                val request = RegisterRequest(
                    displayName    = binding.etDisplayName.text.toString().trim(),
                    username       = binding.etUsername.text.toString().trim(),
                    phoneNumber    = binding.etPhone.text.toString().trim(),
                    whatsappNumber = binding.etWhatsapp.text.toString().trim().ifEmpty { null },
                    email          = binding.etEmail.text.toString().trim().ifEmpty { null },
                    password       = binding.etPassword.text.toString().trim(),
                    userType       = selectedUserType,
                    referralId     = binding.etReferralId.text.toString().trim().ifEmpty { null },
                    address        = binding.etAddress.text.toString().trim().ifEmpty { null },
                    deviceName     = android.os.Build.MODEL,
                    appVersion     = "1.0"
                )
                viewModel.register(request)
            }
        }

        binding.tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.registerState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnRegister.isEnabled = false
                    binding.btnRegister.text = "Creating account…"
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "CREATE ACCOUNT"
                    Snackbar.make(
                        binding.root,
                        "Account created successfully! 🎉",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "CREATE ACCOUNT"
                    Snackbar.make(binding.root, resource.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.etDisplayName.text.toString().trim().isEmpty()) {
            binding.tilDisplayName.error = "Display name is required"
            isValid = false
        } else binding.tilDisplayName.error = null

        if (binding.etUsername.text.toString().trim().isEmpty()) {
            binding.tilUsername.error = "Username is required"
            isValid = false
        } else binding.tilUsername.error = null

        val phone = binding.etPhone.text.toString().trim()
        if (phone.isEmpty()) {
            binding.tilPhone.error = "Phone number is required"
            isValid = false
        } else if (phone.length < 10) {
            binding.tilPhone.error = "Enter valid phone number"
            isValid = false
        } else binding.tilPhone.error = null

        val password = binding.etPassword.text.toString().trim()
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Minimum 6 characters"
            isValid = false
        } else binding.tilPassword.error = null

        return isValid
    }
}