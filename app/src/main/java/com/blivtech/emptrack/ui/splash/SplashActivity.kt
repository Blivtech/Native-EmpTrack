package com.blivtech.emptrack.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blivtech.emptrack.databinding.ActivitySplashBinding
import com.blivtech.emptrack.ui.home.HomeActivity
import com.blivtech.emptrack.ui.login.LoginActivity
import com.blivtech.emptrack.ui.offline.OfflineActivity
import com.blivtech.emptrack.utils.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels()

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animateViews()

        // ✅ After animation check login state
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginAndNavigate()
        }, 2500)
    }

    private fun checkLoginAndNavigate() {
        lifecycleScope.launch {
            val isLoggedIn = preferenceManager.isLoggedIn.first()
            val isOnline   = viewModel.checkNetworkSync()

            when {
                // ✅ Not logged in → go to Login
                !isLoggedIn -> {
                    navigateTo(LoginActivity::class.java)
                }

                // ✅ Logged in but offline → go to Offline screen
                !isOnline -> {
                    navigateTo(OfflineActivity::class.java)
                }

                // ✅ Logged in + online → go directly to Home
                else -> {
                    val btCode = preferenceManager.btCode.first()
                    navigateToHome(btCode)
                }
            }
        }
    }

    private fun <T> navigateTo(destination: Class<T>) {
        startActivity(Intent(this, destination))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun navigateToHome(btCode: String) {
        startActivity(
            Intent(this, HomeActivity::class.java).apply {
                putExtra("btCode", btCode)
                putExtra("fromLogin", false)    // ✅ Not from login — skip sync
            }
        )
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun animateViews() {
        binding.ivLogo.animate()
            .alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(700).setStartDelay(300).start()

        binding.tvAppName.translationY = 50f
        binding.tvAppName.animate()
            .alpha(1f).translationY(0f)
            .setDuration(600).setStartDelay(700).start()

        binding.tvTagline.animate()
            .alpha(1f).setDuration(600).setStartDelay(1000).start()

        binding.tvVersion.animate()
            .alpha(1f).setDuration(400).setStartDelay(1200).start()

        binding.progressBar.animate()
            .alpha(1f).setDuration(400).setStartDelay(1400).start()
    }
}