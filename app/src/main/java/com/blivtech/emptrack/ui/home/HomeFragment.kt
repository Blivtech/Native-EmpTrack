package com.blivtech.emptrack.ui.home

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.CompanyEntity
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.data.model.Banner
import com.blivtech.emptrack.databinding.FragmentHomeBinding
import com.blivtech.emptrack.ui.advance.AddEntryActivity
import com.blivtech.emptrack.ui.product.ProductListActivity
import com.blivtech.emptrack.ui.shiftplan.ShiftPlanActivity
import com.blivtech.emptrack.ui.workers.DailyEntryActivity
import com.blivtech.emptrack.utils.PreferenceManager
import com.blivtech.emptrack.utils.SyncEventBus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DashboardAdapter
    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var currentCompany: CompanyEntity? = null
    private var currentShifts  = listOf<ShiftEntity>()
    private var selectedCompanyCode = ""
    private var selectedCompanyName = ""
    private var btCode = ""

    // ── Banner carousel ──
    private lateinit var bannerAdapter: BannerAdapter
    private var bannerCount = 0
    private val bannerHandler = Handler(Looper.getMainLooper())
    private val bannerRunnable = object : Runnable {
        override fun run() {
            if (bannerCount == 0) return
            val next = (binding.vpBanner.currentItem + 1) % bannerCount
            binding.vpBanner.setCurrentItem(next, true)
            bannerHandler.postDelayed(this, 4200)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setGreeting()
        setupBanners()
        setupModules()
        setupAvatarClick()
        observeData()
        observeSyncBus()
    }

    // ─────────────────────────────────
    // ✅ Greeting
    // ─────────────────────────────────
    private fun setGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hour < 12 -> "Good morning,"
            hour < 17 -> "Good afternoon,"
            else      -> "Good evening,"
        }
    }

    // ─────────────────────────────────
    // ✅ Ad / announcement carousel
    // ─────────────────────────────────
    private fun setupBanners() {
        val banners = listOf(
            Banner("New", "Monthly reports, one tap",
                "Export attendance & piece-rate pay to PDF or Excel.",
                "Try it", R.drawable.bg_banner_blue),
            Banner("Coming soon", "Biometric integration",
                "Pull attendance straight from your device.",
                "Learn more", R.drawable.bg_banner_teal),
            Banner("Live now", "Work-Based Pay is on",
                "Log pieces — rates & names resolve automatically.",
                "Open module", R.drawable.bg_banner_purple)
        )
        bannerCount = banners.size

        bannerAdapter = BannerAdapter(banners) { banner ->
            // TODO: route the CTA (e.g. open a module / report screen)
        }

        binding.vpBanner.apply {
            adapter = bannerAdapter
            offscreenPageLimit = 1
            clipToPadding = false
            clipChildren = false
            // peek + gap like the mock
            (getChildAt(0) as RecyclerView).apply {
                setPadding(dp(16), 0, dp(16), 0)
                clipToPadding = false
            }
            setPageTransformer(MarginPageTransformer(dp(12)))
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) = updateDots(position)
            })
        }

        buildDots(bannerCount)
    }

    private fun buildDots(count: Int) {
        binding.dotsContainer.removeAllViews()
        repeat(count) {
            val dot = View(requireContext())
            val lp = LinearLayout.LayoutParams(dp(7), dp(7)).apply { marginEnd = dp(6) }
            dot.layoutParams = lp
            dot.background = makeDot(false)
            binding.dotsContainer.addView(dot)
        }
        updateDots(0)
    }

    private fun updateDots(selected: Int) {
        for (i in 0 until binding.dotsContainer.childCount) {
            val dot = binding.dotsContainer.getChildAt(i)
            val lp = dot.layoutParams as LinearLayout.LayoutParams
            lp.width = if (i == selected) dp(20) else dp(7)
            dot.layoutParams = lp
            dot.background = makeDot(i == selected)
        }
    }

    private fun makeDot(active: Boolean) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = dp(4).toFloat()
        setColor(Color.parseColor(if (active) "#2563EB" else "#C3CEDE"))
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun setupAvatarClick() {
        binding.tvAvatar.setOnClickListener {
            (activity as? HomeActivity)?.openDrawer()
        }
        binding.ivSync.setOnClickListener {
            (activity as? HomeActivity)?.let {
                it.javaClass.getDeclaredMethod("showSyncDialog")
                    .apply { isAccessible = true }
                    .invoke(it)
            }
        }
    }

    // ─────────────────────────────────
    // ✅ Modules grid — now 3 columns, compact tiles
    // ─────────────────────────────────
    private fun setupModules() {
        adapter = DashboardAdapter(viewModel.items) { card ->
            navigateToModule(card.title)
        }
        binding.rvModules.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            this.adapter  = this@HomeFragment.adapter
        }
    }

    private fun navigateToModule(name: String) {
        val navController = findNavController()
        when (name) {
            "Employee"   -> navController.navigate(R.id.employeeFragment)
            "Attendance" -> navController.navigate(R.id.attendanceFragment)
            "Reports"    -> navController.navigate(R.id.reportsFragment)

            "Product" -> startActivity(
                Intent(requireContext(), ProductListActivity::class.java).apply {
                    putExtra("companyCode", currentCompany?.companyCode ?: "")
                }
            )
            "Salary" -> { /* TODO */ }
            "Extra Pay & Advances" -> startActivity(
                Intent(requireContext(), AddEntryActivity::class.java).apply {
                    putExtra("btCode", btCode)
                    putExtra("companyName", selectedCompanyName)
                    putExtra("companyCode", selectedCompanyCode)
                }
            )
            "Work-Based Pay" -> startActivity(
                Intent(requireContext(), DailyEntryActivity::class.java).apply {
                    putExtra("btCode", btCode)
                    putExtra("companyName", selectedCompanyName)
                    putExtra("companyCode", selectedCompanyCode)
                }
            )
            "Shift Management" -> startActivity(
                Intent(requireContext(), ShiftPlanActivity::class.java)
            )
        }
    }

    // ─────────────────────────────────
    // ✅ Observe data
    // ─────────────────────────────────
    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val savedCompanyCode = preferenceManager.selectedCompanyCode.first()

            viewModel.getCompanies().observe(viewLifecycleOwner) { companies ->
                if (companies.isNotEmpty()) {
                    val company = companies.find {
                        it.companyCode == savedCompanyCode
                    } ?: companies.first()
                    currentCompany = company
                } else {
                    binding.tvCompanyName.text = "No company — tap to add"
                }
            }

            val name = preferenceManager.userName.first()
            val initials = name.split(" ")
                .take(2).joinToString("") { it.first().uppercase() }
            binding.tvUserName.text = name
            binding.tvAvatar.text   = initials
        }
    }

    private fun observeSyncBus() {
        SyncEventBus.syncCompleted.observe(viewLifecycleOwner) {
            observeData()
        }
    }

    override fun onResume() {
        super.onResume()
        bannerHandler.postDelayed(bannerRunnable, 4200)   // start auto-scroll
        viewLifecycleOwner.lifecycleScope.launch {
            btCode = preferenceManager.btCode.first()
            selectedCompanyCode = preferenceManager.selectedCompanyCode.first()
            selectedCompanyName = preferenceManager.selectedCompanyName.first()
            binding.tvCompanyName.text = selectedCompanyName
        }
    }

    override fun onPause() {
        super.onPause()
        bannerHandler.removeCallbacks(bannerRunnable)      // stop auto-scroll
    }

    override fun onDestroyView() {
        bannerHandler.removeCallbacks(bannerRunnable)
        super.onDestroyView()
        _binding = null
    }
}
