package com.blivtech.emptrack.ui.signup

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.R
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

        setupUserType()
        setupClickListeners()
        observeViewModel()
        setupFieldFocus()
        setupPasswordToggle()
    }



    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
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
                    binding.btnSignUp.isEnabled = false
                    binding.btnSignUp.text = "Creating account…"
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSignUp.isEnabled = true
                    binding.btnSignUp.text = "CREATE ACCOUNT"
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
                    binding.btnSignUp.isEnabled = true
                    binding.btnSignUp.text = "CREATE ACCOUNT"
                    Snackbar.make(binding.root, resource.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }



    private var passwordVisible = false

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

    private fun setupUserType() {
        val types = listOf("Admin", "Manager", "Staff")   // <-- use your real list
        binding.actvUserType.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, types)
        )
        binding.actvUserType.setOnClickListener { binding.actvUserType.showDropDown() }
        binding.actvUserType.setOnItemClickListener { _, _, pos, _ ->
            clearFieldError(binding.llUserType, binding.tvUserTypeError)
            // selectedUserType = types[pos]
        }
    }

    // --- Focus ring + clear error on focus ---
    private fun setupFieldFocus() {
        val fields = listOf(
            Triple(binding.etDisplayName, binding.llDisplayName, binding.tvDisplayNameError),
            Triple(binding.etUsername,    binding.llUsername,    binding.tvUsernameError),
            Triple(binding.etPhone,       binding.llPhone,       binding.tvPhoneError),
            Triple(binding.etPassword,    binding.llPassword,    binding.tvPasswordError)
        )
        fields.forEach { (edit, box, err) ->
            edit.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) { err.visibility = View.GONE; box.setBackgroundResource(R.drawable.bg_field_focused) }
                else box.setBackgroundResource(R.drawable.bg_field)
            }
        }
        // non-required fields just toggle the border
        listOf(binding.llWhatsapp to binding.etWhatsapp, binding.llEmail to binding.etEmail,
            binding.llAddress to binding.etAddress, binding.llReferral to binding.etReferralId
        ).forEach { (box, edit) ->
            edit.setOnFocusChangeListener { _, has ->
                box.setBackgroundResource(if (has) R.drawable.bg_field_focused else R.drawable.bg_field)
            }
        }
    }

    // --- Validation (replaces any old tilX.error calls) ---
    private fun validateInputs(): Boolean {
        var ok = true
        fun req(box: View, err: TextView, value: String, msg: String): Boolean {
            return if (value.isBlank()) { showFieldError(box, err, msg); false }
            else { clearFieldError(box, err); true }
        }
        ok = req(binding.llDisplayName, binding.tvDisplayNameError, binding.etDisplayName.text.toString().trim(), "Display name is required") and ok
        ok = req(binding.llUsername,    binding.tvUsernameError,    binding.etUsername.text.toString().trim(),    "Username is required") and ok

        val phone = binding.etPhone.text.toString().trim()
        ok = when {
            phone.isEmpty()   -> { showFieldError(binding.llPhone, binding.tvPhoneError, "Phone number is required"); false }
            phone.length < 10 -> { showFieldError(binding.llPhone, binding.tvPhoneError, "Enter valid phone number"); false }
            else              -> { clearFieldError(binding.llPhone, binding.tvPhoneError); true }
        } and ok

        val pass = binding.etPassword.text.toString().trim()
        ok = when {
            pass.isEmpty()  -> { showFieldError(binding.llPassword, binding.tvPasswordError, "Password is required"); false }
            pass.length < 6 -> { showFieldError(binding.llPassword, binding.tvPasswordError, "Minimum 6 characters"); false }
            else            -> { clearFieldError(binding.llPassword, binding.tvPasswordError); true }
        } and ok

        if (binding.actvUserType.text.isNullOrBlank()) {
            showFieldError(binding.llUserType, binding.tvUserTypeError, "Select a user type"); ok = false
        } else clearFieldError(binding.llUserType, binding.tvUserTypeError)

        return ok
    }

    private fun showFieldError(box: View, err: TextView, msg: String) {
        box.setBackgroundResource(R.drawable.bg_field_error); err.text = msg; err.visibility = View.VISIBLE
    }
    private fun clearFieldError(box: View, err: TextView) {
        box.setBackgroundResource(R.drawable.bg_field); err.visibility = View.GONE
    }
}