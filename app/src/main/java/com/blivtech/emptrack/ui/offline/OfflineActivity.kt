package com.blivtech.emptrack.ui.offline

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.databinding.ActivityOfflineBinding
import com.blivtech.emptrack.ui.login.LoginActivity
import com.blivtech.emptrack.ui.splash.SplashViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OfflineActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOfflineBinding
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animateViews()
        setupClickListeners()
        observeViewModel()
    }

    private fun animateViews() {
        // Icon bounce
        binding.ivOffline.animate()
            .alpha(1f).setDuration(600).setStartDelay(200).start()

        // Title slide up
        binding.tvOfflineTitle.translationY = 30f
        binding.tvOfflineTitle.animate()
            .alpha(1f).translationY(0f)
            .setDuration(500).setStartDelay(400).start()

        // Subtitle fade
        binding.tvOfflineSubtitle.animate()
            .alpha(1f).setDuration(500).setStartDelay(600).start()

        // Tips fade
        binding.layoutTips.animate()
            .alpha(1f).setDuration(500).setStartDelay(800).start()
    }

    private fun setupClickListeners() {
        // Retry button
        binding.btnRetry.setOnClickListener {
            binding.btnRetry.text = "Checking…"
            binding.btnRetry.isEnabled = false
            viewModel.checkNetwork()
        }

        // Open Settings button
        binding.btnOpenSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
    }

    private fun observeViewModel() {
        viewModel.isOnline.observe(this) { isOnline ->
            if (isOnline) {
                // ✅ Back online → Go to Login
                Snackbar.make(binding.root, "Connected! 🎉", Snackbar.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            } else {
                // ❌ Still offline
                binding.btnRetry.text = "Try Again"
                binding.btnRetry.isEnabled = true
                Snackbar.make(
                    binding.root,
                    "Still no internet. Please check your connection.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
}