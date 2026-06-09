package com.blivtech.emptrack.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blivtech.emptrack.data.model.LoginRequest
import com.blivtech.emptrack.databinding.ActivityLoginBinding
import com.blivtech.emptrack.ui.signup.SignUpActivity
import com.blivtech.emptrack.ui.home.HomeActivity
import com.blivtech.emptrack.utils.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                val request = LoginRequest(
                    phoneNumber = binding.etPhone.text.toString().trim(),
                    password    = binding.etPassword.text.toString().trim(),
                    deviceName  = android.os.Build.MODEL,
                    appVersion  = "1.0"
                )
                viewModel.login(request)
            }
          //  startActivity(Intent(this, HomeActivity::class.java))
        }

        binding.tvGoToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            Snackbar.make(binding.root, "Feature coming soon!", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                    binding.btnLogin.text = "Signing in…"
                }

                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "SIGN IN"

                    val data = resource.data

                    // ✅ Save login data to DataStore
                    lifecycleScope.launch {
                        viewModel.saveLoginData(
                            token    = data.token,
                            btCode   = data.btCode,
                            userName = data.displayName,
                            phone    = data.phoneNumber,
                            userType = data.userType
                        )

                        // ✅ Navigate to Home with fromLogin = true
                        startActivity(
                            Intent(this@LoginActivity, HomeActivity::class.java).apply {
                                putExtra("btCode", data.btCode)
                                putExtra("fromLogin", true)     // ✅ Trigger sync
                            }
                        )
                        overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )
                        finish()
                    }
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "SIGN IN"
                    Snackbar.make(binding.root, resource.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

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