package com.blivtech.emptrack.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blivtech.emptrack.R
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
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeViewModel()
        setupPasswordToggle()
        setupFieldFocus()
    }

    private fun setupPasswordToggle() {
        binding.ivTogglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            binding.etPassword.inputType = if (passwordVisible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.etPassword.typeface = binding.etPhone.typeface
            binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
            binding.ivTogglePassword.setImageResource(
                if (passwordVisible) R.drawable.ic_eye_off else R.drawable.ic_eye
            )
        }
    }

    // Blue focus ring + clears the error when the user taps in to fix it
    private fun setupFieldFocus() {
        val fields = listOf(
            Triple(binding.etPhone,    binding.llPhone,    binding.tvPhoneError),
            Triple(binding.etPassword, binding.llPassword, binding.tvPasswordError)
        )
        fields.forEach { (edit, box, err) ->
            edit.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    err.visibility = View.GONE
                    box.setBackgroundResource(R.drawable.bg_field_focused)
                } else {
                    box.setBackgroundResource(R.drawable.bg_field)
                }
            }
        }
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
                    lifecycleScope.launch {
                        viewModel.saveLoginData(
                            token    = data.token,
                            btCode   = data.btCode,
                            userName = data.displayName,
                            phone    = data.phoneNumber,
                            userType = data.userType
                        )
                        startActivity(
                            Intent(this@LoginActivity, HomeActivity::class.java).apply {
                                putExtra("btCode", data.btCode)
                                putExtra("fromLogin", true)
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

    // ── Validation on the custom icon-chip fields (no TextInputLayout) ──
    private fun validateInputs(): Boolean {
        var isValid = true

        val phone = binding.etPhone.text.toString().trim()
        when {
            phone.isEmpty()   -> { showFieldError(binding.llPhone, binding.tvPhoneError, "Phone number is required"); isValid = false }
            phone.length < 10 -> { showFieldError(binding.llPhone, binding.tvPhoneError, "Enter valid phone number"); isValid = false }
            else              -> clearFieldError(binding.llPhone, binding.tvPhoneError)
        }

        val password = binding.etPassword.text.toString().trim()
        when {
            password.isEmpty()  -> { showFieldError(binding.llPassword, binding.tvPasswordError, "Password is required"); isValid = false }
            password.length < 6 -> { showFieldError(binding.llPassword, binding.tvPasswordError, "Minimum 6 characters"); isValid = false }
            else                -> clearFieldError(binding.llPassword, binding.tvPasswordError)
        }

        return isValid
    }

    private fun showFieldError(box: View, errorView: TextView, message: String) {
        box.setBackgroundResource(R.drawable.bg_field_error)
        errorView.text = message
        errorView.visibility = View.VISIBLE
    }

    private fun clearFieldError(box: View, errorView: TextView) {
        box.setBackgroundResource(R.drawable.bg_field)
        errorView.visibility = View.GONE
    }
}