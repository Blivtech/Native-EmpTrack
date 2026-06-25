package com.blivtech.emptrack.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.CompanyEntity
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.databinding.FragmentHomeBinding
import com.blivtech.emptrack.ui.contract.ContractWageActivity
import com.blivtech.emptrack.ui.entry.AddEntryActivity

import com.blivtech.emptrack.ui.shiftplan.ShiftPlanActivity
import com.blivtech.emptrack.ui.work.AddWorkEntryActivity
import com.blivtech.emptrack.utils.PreferenceManager
import com.blivtech.emptrack.utils.SyncEventBus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ModuleCardAdapter
    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var currentCompany: CompanyEntity? = null
    private var currentShifts  = listOf<ShiftEntity>()
    private var selectedCompanyCode = ""
    private var selectedCompanyName = ""
    private var btCode = ""

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
        setTodayDate()
        setupModules()
        setupAvatarClick()
        observeData()
        observeSyncBus()
    }

    // ─────────────────────────────────
    // ✅ Greeting & Date
    // ─────────────────────────────────
    private fun setGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hour < 12 -> "Good morning,"
            hour < 17 -> "Good afternoon,"
            else      -> "Good evening,"
        }
    }

    private fun setTodayDate() {
        binding.tvTodayDate.text =
            SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
                .format(Date())
    }

    // ─────────────────────────────────
    // ✅ Avatar → opens drawer (in host Activity)
    // ─────────────────────────────────
    private fun setupAvatarClick() {
        binding.tvAvatar.setOnClickListener {
            (activity as? HomeActivity)?.openDrawer()
        }
        binding.ivSync.setOnClickListener {
            // ✅ Re-trigger sync via MainActivity
            (activity as? HomeActivity)?.let {
                it.javaClass.getDeclaredMethod("showSyncDialog")
                    .apply { isAccessible = true }
                    .invoke(it)
            }
        }
    }

    // ─────────────────────────────────
    // ✅ Modules grid
    // ─────────────────────────────────
    private fun setupModules() {
        adapter = ModuleCardAdapter(viewModel.moduleCards) { card ->
            navigateToModule(card.cardName)
        }
        binding.rvModules.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            this.adapter  = this@HomeFragment.adapter
        }
    }

    private fun navigateToModule(name: String) {
        val navController = findNavController()
        when (name) {
            "Employee"   -> navController.navigate(R.id.employeeFragment)
            "Attendance" -> navController.navigate(R.id.attendanceFragment)
            "Reports"    -> navController.navigate(R.id.reportsFragment)

            "Work Progress" -> startActivity(
                Intent(requireContext(), ContractWageActivity::class.java).apply {
                    putExtra("companyCode", currentCompany?.companyCode ?: "")
                }
            )
            "Salary" -> { /* TODO */ }
            "Advance" -> startActivity(
                Intent(requireContext(), AddEntryActivity::class.java).apply {
                    putExtra("btCode", btCode)
                    putExtra("companyName", selectedCompanyName)
                    putExtra("companyCode", selectedCompanyCode)
                }
            )
            "Inventory" -> { /* TODO */ }
            "Shift Mgmt" -> startActivity(
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
                    binding.tvCompanyName.text =
                        "${company.name} · ${company.city ?: ""}"
                    binding.tvTotalEmp.text = "50"

                    viewModel.getShifts(company.companyCode)
                        .observe(viewLifecycleOwner) { shifts ->
                            currentShifts = shifts
                            updateShiftCards(shifts)
                        }
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

    private fun updateShiftCards(shifts: List<ShiftEntity>) {
        binding.layoutShifts.removeAllViews()
        if (shifts.isEmpty()) {
            binding.tvActiveShift.text = "No shifts"
            return
        }
        shifts.forEachIndexed { index, shift ->
            val shiftView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_shift_row, binding.layoutShifts, false)
            shiftView.findViewById<TextView>(R.id.tvShiftRowName).text =
                "Shift ${index + 1} · ${shift.shiftName} · " +
                "${shift.startTime.take(5)}–${shift.endTime.take(5)}"
            binding.layoutShifts.addView(shiftView)
        }
        binding.tvActiveShift.text = "Shift 1 Active"
    }

    // ─────────────────────────────────
    // ✅ Refresh after sync completes (replaces old observeData() re-call)
    // ─────────────────────────────────
    private fun observeSyncBus() {
        SyncEventBus.syncCompleted.observe(viewLifecycleOwner) {
            observeData()
        }
    }

    // ─────────────────────────────────
    // ✅ Refresh on return — like old onResume()
    // ─────────────────────────────────
    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            btCode=preferenceManager.btCode.first()
            selectedCompanyCode = preferenceManager.selectedCompanyCode.first()
            selectedCompanyName = preferenceManager.selectedCompanyName.first()
            binding.tvCompanyName.text = selectedCompanyName

            viewModel.getShifts(selectedCompanyCode)
                .observe(viewLifecycleOwner) { shifts ->
                    currentShifts = shifts
                    updateShiftCards(shifts)
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}