package com.blivtech.emptrack.ui.attendance

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.databinding.ActivityMarkAttendanceBinding
import com.blivtech.emptrack.databinding.DialogConfirmAttendanceBinding
import com.blivtech.emptrack.databinding.DialogDeviationBinding
import com.blivtech.emptrack.ui.attendance.adapter.AttendanceEmployeeAdapter
import com.blivtech.emptrack.ui.attendance.adapter.ConfirmAttendanceAdapter
import com.blivtech.emptrack.ui.attendance.adapter.DeviationAdapter
import com.blivtech.emptrack.utils.PreferenceManager
import com.blivtech.emptrack.utils.Resource
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MarkAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarkAttendanceBinding
    private val viewModel: MarkAttendanceViewModel by viewModels()
    private lateinit var adapter: AttendanceEmployeeAdapter

    @Inject
    lateinit var preferenceManager: PreferenceManager

    // ✅ Intent extras
    private val btCode by lazy { intent.getStringExtra("btCode") ?: "" }
    private val companyCode by lazy { intent.getStringExtra("companyCode") ?: "" }
    private val companyName by lazy { intent.getStringExtra("companyName") ?: "" }
    private val shiftCode by lazy { intent.getStringExtra("shiftCode") ?: "" }
    private val shiftName by lazy { intent.getStringExtra("shiftName") ?: "" }
    private val shiftStartTime by lazy { intent.getStringExtra("shiftStartTime") ?: "" }
    private val shiftEndTime by lazy { intent.getStringExtra("shiftEndTime") ?: "" }
    private val date by lazy { intent.getStringExtra("date") ?: "" }
    private val mode by lazy { intent.getStringExtra("mode") ?: "NEW" }
    private val attendanceId by lazy { intent.getStringExtra("attendanceId") }

    // ✅ Tab constants
    companion object {
        const val TAB_UNSELECTED = 0
        const val TAB_ALL        = 1
        const val TAB_WORKING    = 2
        const val TAB_WEEKOFF    = 3
        const val TAB_LEAVE      = 4
        const val TAB_HOLIDAY    = 5
    }

    private var currentTab   = TAB_UNSELECTED
    private var allEmployees = listOf<EmployeeEntity>()
    private var searchQuery  = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarkAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        setupTabs()
        setupSearch()
        observeData()

        viewModel.loadEmployees(companyCode, shiftCode)

        if (mode == "EDIT" && attendanceId != null) {
            viewModel.loadExistingAttendance(attendanceId!!)
        } else {
            // ✅ Load yesterday for prefill option
            viewModel.loadYesterdayAttendance(btCode, companyCode, shiftCode)
        }
    }

    // ─────────────────────────────────────
    // ✅ Setup UI
    // ─────────────────────────────────────
    private fun setupUI() {
        binding.ivBack.setOnClickListener { finish() }
        binding.tvShiftBanner.text = "$shiftName · $companyName"
        binding.tvShiftTime.text   =
            "${shiftStartTime.take(5)} – ${shiftEndTime.take(5)} · $date"
        binding.tvProgress.text    = "0/0"
        binding.btnSubmit.text     = if (mode == "EDIT")
            "Update attendance →" else "Submit attendance →"

        // ✅ Deviation button
        binding.btnDeviation.setOnClickListener {
            showDeviationSheet()
        }

        // ✅ Prefill button
        binding.btnPrefill.setOnClickListener {
            viewModel.prefillFromYesterday()
            filterAndRefresh()
            updateBottomBar()
            binding.layoutPrefillBanner.visibility = View.VISIBLE
            binding.btnPrefill.visibility = View.GONE
            Snackbar.make(
                binding.root,
                "Pre-filled from yesterday's attendance!",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    // ─────────────────────────────────────
    // ✅ Setup RecyclerView
    // ─────────────────────────────────────
    private fun setupRecyclerView() {
        adapter = AttendanceEmployeeAdapter { empCode, status, workType ->
            viewModel.updateStatus(empCode, status, workType)
            updateBottomBar()
            filterAndRefresh()
        }
        binding.rvEmployees.layoutManager = LinearLayoutManager(this)
        binding.rvEmployees.adapter       = adapter
    }

    // ─────────────────────────────────────
    // ✅ Setup Tabs
    // ─────────────────────────────────────
    private fun setupTabs() {
        listOf(
            binding.tabUnselected,
            binding.tabAll,
            binding.tabWorking,
            binding.tabWeekoff,
            binding.tabLeave,
            binding.tabHoliday
        ).forEachIndexed { index, tab ->
            tab.setOnClickListener {
                currentTab = index
                updateTabUI()
                filterAndRefresh()
            }
        }
        updateTabUI()
    }

    private fun updateTabUI() {
        listOf(
            binding.tabUnselected,
            binding.tabAll,
            binding.tabWorking,
            binding.tabWeekoff,
            binding.tabLeave,
            binding.tabHoliday
        ).forEachIndexed { index, tab ->
            if (index == currentTab) {
                tab.setBackgroundResource(R.drawable.bg_tab_active)
                tab.setTextColor(getColor(R.color.blue_dark))
            } else {
                tab.setBackgroundResource(R.drawable.bg_tab_inactive)
                tab.setTextColor(getColor(android.R.color.darker_gray))
            }
        }
    }

    // ─────────────────────────────────────
    // ✅ Setup Search
    // ─────────────────────────────────────
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim()?.lowercase() ?: ""
                binding.ivClearSearch.visibility =
                    if (searchQuery.isNotEmpty()) View.VISIBLE else View.GONE
                filterAndRefresh()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.ivClearSearch.setOnClickListener {
            binding.etSearch.setText("")
            searchQuery = ""
            binding.ivClearSearch.visibility = View.GONE
            filterAndRefresh()
        }
    }

    // ─────────────────────────────────────
    // ✅ Observe Data
    // ─────────────────────────────────────
    private fun observeData() {

        // ✅ Show/hide deviation button
        viewModel.showDeviation.observe(this) { show ->
            binding.btnDeviation.visibility =
                if (show) View.VISIBLE else View.GONE
        }

        // ✅ Employees loaded
        viewModel.employees.observe(this) { employees ->
            allEmployees = employees
            filterAndRefresh()
            updateBottomBar()

            if (employees.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvEmployees.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvEmployees.visibility = View.VISIBLE
            }
        }

        // ✅ Yesterday attendance loaded — show prefill button
        viewModel.yesterdayAttendance.observe(this) { resource ->
            if (resource is Resource.Success && resource.data.isMarked) {
                binding.btnPrefill.visibility = View.VISIBLE
            } else {
                binding.btnPrefill.visibility = View.GONE
            }
        }

        // ✅ EDIT mode — prefill existing
        viewModel.existingAttendance.observe(this) { resource ->
            if (resource is Resource.Success) {
                val att = resource.data
                if (att.isMarked && att.employees != null) {
                    att.employees.forEach { detail ->
                        viewModel.updateStatus(
                            empCode       = detail.empCode,
                            dayPlanStatus = detail.dayPlanStatus,
                            workType      = detail.workType,
                            remarks       = detail.remarks ?: ""
                        )
                    }
                    filterAndRefresh()
                    updateBottomBar()
                }
            }
        }

        viewModel.submitState.observe(this) { resource ->
            resource ?: return@observe  // ✅ Skip if null

            when (resource) {
                is Resource.Loading -> {
                    binding.btnSubmit.isEnabled = false
                    binding.btnSubmit.text      = "Submitting..."
                }
                is Resource.Success -> {
                    Snackbar.make(
                        binding.root,
                        if (mode == "EDIT") "Attendance updated! ✅"
                        else "Attendance submitted! ✅",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    finish()
                }
                is Resource.Error -> {
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.text      = if (mode == "EDIT")
                        "Update attendance →" else "Submit attendance →"
                    Snackbar.make(
                        binding.root,
                        resource.message,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // ─────────────────────────────────────
    // ✅ Filter and refresh
    // ─────────────────────────────────────
    private fun filterAndRefresh() {
        var filtered = when (currentTab) {
            TAB_UNSELECTED -> allEmployees.filter {
                !viewModel.statusMap.containsKey(it.empCode)
            }
            TAB_ALL        -> allEmployees
            TAB_WORKING    -> allEmployees.filter {
                viewModel.statusMap[it.empCode]?.dayPlanStatus == 1
            }
            TAB_WEEKOFF    -> allEmployees.filter {
                viewModel.statusMap[it.empCode]?.dayPlanStatus == 2
            }
            TAB_LEAVE      -> allEmployees.filter {
                viewModel.statusMap[it.empCode]?.dayPlanStatus == 3
            }
            TAB_HOLIDAY    -> allEmployees.filter {
                viewModel.statusMap[it.empCode]?.dayPlanStatus == 4
            }
            else           -> allEmployees
        }

        // ✅ Apply search
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter { emp ->
                emp.name.lowercase().contains(searchQuery) ||
                        emp.empCode.lowercase().contains(searchQuery) ||
                        (emp.phone?.lowercase()?.contains(searchQuery) == true)
            }
        }

        // ✅ Sort — unselected first in All tab
        val sorted = if (currentTab == TAB_ALL) {
            filtered.sortedWith(
                compareBy<EmployeeEntity> {
                    viewModel.statusMap.containsKey(it.empCode)
                }.reversed().thenBy { it.name }
            )
        } else {
            filtered.sortedBy { it.name }
        }

        adapter.submitList(sorted, viewModel.statusMap)
        updateTabCounts()
    }

    // ─────────────────────────────────────
    // ✅ Update tab counts
    // ─────────────────────────────────────
    private fun updateTabCounts() {
        val total      = allEmployees.size
        val unselected = total - viewModel.statusMap.size
        val working    = viewModel.getCount(1)
        val weekoff    = viewModel.getCount(2)
        val leave      = viewModel.getCount(3)
        val holiday    = viewModel.getCount(4)

        binding.tabUnselected.text = "Pending ($unselected)"
        binding.tabAll.text        = "All ($total)"
        binding.tabWorking.text    = "Working ($working)"
        binding.tabWeekoff.text    = "Off ($weekoff)"
        binding.tabLeave.text      = "Leave ($leave)"
        binding.tabHoliday.text    = "Holiday ($holiday)"
    }

    // ─────────────────────────────────────
    // ✅ Update bottom bar
    // ─────────────────────────────────────
    private fun updateBottomBar() {
        val total   = allEmployees.size
        val marked  = viewModel.statusMap.size
        val pending = total - marked

        binding.tvProgress.text     = "$marked/$total"
        binding.tvCountWorking.text = viewModel.getCount(1).toString()
        binding.tvCountWeekoff.text = viewModel.getCount(2).toString()
        binding.tvCountLeave.text   = viewModel.getCount(3).toString()
        binding.tvCountHoliday.text = viewModel.getCount(4).toString()
        binding.tvCountPending.text = pending.toString()

        if (total > 0 && viewModel.allMarked()) {
            binding.btnSubmit.isEnabled = true
            binding.btnSubmit.text      = if (mode == "EDIT")
                "Update attendance →" else "Submit attendance →"
            binding.btnSubmit.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    getColor(android.R.color.holo_blue_dark)
                )
            binding.btnSubmit.setOnClickListener { showConfirmDialog() }
        } else {
            binding.btnSubmit.isEnabled = false
            binding.btnSubmit.text      = "$pending employees pending"
            binding.btnSubmit.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    getColor(android.R.color.darker_gray)
                )
        }
    }

    // ─────────────────────────────────────
    // ✅ Deviation bottom sheet
    // ─────────────────────────────────────
    private fun showDeviationSheet() {
        val dialogBinding = DialogDeviationBinding
            .inflate(LayoutInflater.from(this))
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogBinding.root)

        val deviationEmps = viewModel.getDeviationEmployees()
        val selectedCodes = mutableSetOf<String>()

        val deviationAdapter = DeviationAdapter { empCode, isSelected ->
            if (isSelected) selectedCodes.add(empCode)
            else selectedCodes.remove(empCode)
            dialogBinding.tvSelectedCount.text =
                if (selectedCodes.isEmpty()) "Select employees"
                else "${selectedCodes.size} employees selected"
            dialogBinding.btnAddDeviation.isEnabled = selectedCodes.isNotEmpty()
        }

        deviationAdapter.submitList(deviationEmps)
        dialogBinding.rvDeviation.layoutManager = LinearLayoutManager(this)
        dialogBinding.rvDeviation.adapter = deviationAdapter

        // ✅ Search in deviation
        dialogBinding.etDeviationSearch.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val q = s?.toString()?.trim()?.lowercase() ?: ""
                    val filtered = deviationEmps.filter {
                        it.name.lowercase().contains(q) ||
                                it.empCode.lowercase().contains(q)
                    }
                    deviationAdapter.submitList(filtered)
                }
                override fun afterTextChanged(s: Editable?) {}
            }
        )

        dialogBinding.btnCancelDeviation.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnAddDeviation.setOnClickListener {
            viewModel.addDeviationEmployees(selectedCodes.toList())
            dialog.dismiss()
            filterAndRefresh()
            updateBottomBar()
            Snackbar.make(
                binding.root,
                "${selectedCodes.size} employees added!",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        dialog.show()
    }

    // ─────────────────────────────────────
    // ✅ Confirm bottom sheet
    // ─────────────────────────────────────
    private fun showConfirmDialog() {
        val dialogBinding = DialogConfirmAttendanceBinding
            .inflate(LayoutInflater.from(this))
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.tvConfirmTitle.text = if (mode == "EDIT")
            "Update attendance" else "Confirm attendance"
        dialogBinding.tvConfirmShift.text = "$shiftName · $companyName"
        dialogBinding.tvConfirmTime.text  =
            "${shiftStartTime.take(5)} – ${shiftEndTime.take(5)} · $date"
        dialogBinding.tvConfirmWorking.text = viewModel.getCount(1).toString()
        dialogBinding.tvConfirmWeekoff.text = viewModel.getCount(2).toString()
        dialogBinding.tvConfirmLeave.text   = viewModel.getCount(3).toString()
        dialogBinding.tvConfirmHoliday.text = viewModel.getCount(4).toString()
        dialogBinding.tvConfirmTotal.text   = "${allEmployees.size} employees"

        val fullDay = viewModel.statusMap.values.count {
            it.dayPlanStatus == 1 && it.workType == 1
        }
        val halfDay = viewModel.statusMap.values.count {
            it.dayPlanStatus == 1 && it.workType == 2
        }
        dialogBinding.tvWorkingDetail.text = "$fullDay full · $halfDay half day"

        val confirmAdapter = ConfirmAttendanceAdapter()
        val sortedByStatus = allEmployees
            .filter { viewModel.statusMap.containsKey(it.empCode) }
            .sortedBy { viewModel.statusMap[it.empCode]?.dayPlanStatus }
        confirmAdapter.submitList(sortedByStatus, viewModel.statusMap)
        dialogBinding.rvConfirmList.layoutManager = LinearLayoutManager(this)
        dialogBinding.rvConfirmList.adapter       = confirmAdapter

        dialogBinding.btnEdit.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnConfirm.setOnClickListener {
            dialog.dismiss()
            val markedBy = runBlocking {
                preferenceManager.btCode.first().hashCode().toLong()
            }
            viewModel.submit(
                mode         = mode,
                attendanceId = attendanceId,
                btCode       = btCode,
                companyCode  = companyCode,
                shiftCode    = shiftCode,
                date         = date,
                markedBy     = markedBy
            )
        }

        dialog.show()
    }
}