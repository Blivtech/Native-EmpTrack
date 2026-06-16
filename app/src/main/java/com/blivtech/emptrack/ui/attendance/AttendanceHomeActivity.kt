package com.blivtech.emptrack.ui.attendance

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.data.model.ShiftStatusResponse
import com.blivtech.emptrack.databinding.ActivityAttendanceHomeBinding
import com.blivtech.emptrack.utils.PreferenceManager
import com.blivtech.emptrack.utils.Resource
import com.blivtech.emptrack.utils.ShimmerHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AttendanceHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceHomeBinding
    private val viewModel: AttendanceHomeViewModel by viewModels()

    @Inject
    lateinit var preferenceManager: PreferenceManager

    // ✅ Adapter
    private lateinit var shiftCardAdapter: ShiftCardAdapter

    // ✅ State
    private var shifts         = listOf<ShiftEntity>()
    private var todayStatusList = listOf<ShiftStatusResponse>()

    private var btCode      = ""
    private var companyCode = ""
    private var companyName = ""

    private val displayFmt = SimpleDateFormat(
        "EEE, d MMM yyyy", Locale.getDefault()
    )
    private val dateFmt = SimpleDateFormat(
        "yyyy-MM-dd", Locale.getDefault()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            btCode      = preferenceManager.btCode.first()
            companyCode = preferenceManager.selectedCompanyCode.first()
            companyName = preferenceManager.selectedCompanyName.first()

            setupUI()
            setupRecyclerView()
            observeData()

            viewModel.loadEmpCount(companyCode)
            viewModel.loadTodayStatus(btCode, companyCode)
        }
    }

    // ─────────────────────────────────
    // ✅ Setup UI
    // ─────────────────────────────────
    private fun setupUI() {
        binding.tvCompanyName.text = companyName
        binding.tvDate.text        = displayFmt.format(Date())

        binding.ivBack.setOnClickListener { finish() }
        binding.ivCalendar.setOnClickListener {
            // TODO: calendar
        }
    }

    // ─────────────────────────────────
    // ✅ Setup RecyclerView
    // ─────────────────────────────────
    private fun setupRecyclerView() {
        shiftCardAdapter = ShiftCardAdapter(

            // ✅ Mark now clicked
            onMarkClick = { item ->
                openMarkAttendance(item.shift)
            },

            // ✅ Edit clicked
            onEditClick = { item ->
                openMarkAttendance(
                    shift        = item.shift,
                    attendanceId = item.status?.attendanceId,
                    mode         = "EDIT"
                )
            }
        )

        binding.rvShiftCards.apply {
            adapter       = shiftCardAdapter
            layoutManager = LinearLayoutManager(
                this@AttendanceHomeActivity
            )
            // ✅ Disable nested scroll — parent ScrollView handles it
            isNestedScrollingEnabled = false
        }
    }


    private fun observeData() {

        // ✅ Employee count
        viewModel.empCount.observe(this) { count ->
            binding.tvEmpCount.text = "$count employees"
        }

        // ✅ Shifts from Room
        viewModel.getShifts(companyCode).observe(this) { shiftList ->
            shifts = shiftList
            submitShiftCards()
        }

        // ✅ Today's attendance status from API
        viewModel.todayStatus.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    ShimmerHelper.show(
                        binding.shimmerLayout,
                        binding.layoutMain
                    )

                }
                is Resource.Success -> {
                    ShimmerHelper.hide(
                        binding.shimmerLayout,
                        binding.layoutMain     // show RecyclerView
                    )
                    todayStatusList = resource.data
                    submitShiftCards()
                    updateStatsBar()
                }
                is Resource.Error -> {
                    ShimmerHelper.hide(
                        binding.shimmerLayout,
                        binding.layoutMain     // show RecyclerView
                    )
                    todayStatusList = emptyList()
                    submitShiftCards()
                }
            }
        }
    }

    private fun submitShiftCards() {
        if (shifts.isEmpty()) {
            binding.layoutEmpty.visibility  = View.VISIBLE
            binding.rvShiftCards.visibility = View.GONE
            return
        }

        binding.layoutEmpty.visibility  = View.GONE
        binding.rvShiftCards.visibility = View.VISIBLE

        // ✅ Map shifts → ShiftCardItem with status
        val items = shifts.mapIndexed { index, shift ->
            ShiftCardItem(
                shift  = shift,
                index  = index,
                status = todayStatusList.find {
                    it.shiftCode == shift.shiftCode
                }
            )
        }

        // ✅ DiffUtil handles smart updates automatically
        shiftCardAdapter.submitList(items)
    }


    private fun updateStatsBar() {
        var totalPresent  = 0.0
        var totalAbsent   = 0
        var totalOffLeave = 0

        todayStatusList.forEach { status ->
            totalPresent  += status.presentCount
            totalAbsent   += status.absentCount
            totalOffLeave += status.weekoffCount +
                    status.leaveCount   +
                    status.holidayCount
        }

        binding.tvPresent.text  = totalPresent.toInt().toString()
        binding.tvAbsent.text   = totalAbsent.toString()
        binding.tvOffLeave.text = totalOffLeave.toString()
        binding.tvTotalEmp.text =
            (totalOffLeave.toInt() + totalPresent.toInt() + totalAbsent).toString() }

    // ─────────────────────────────────
    // ✅ Open mark attendance
    // ─────────────────────────────────
    private fun openMarkAttendance(
        shift: ShiftEntity,
        attendanceId: String? = null,
        mode: String = "NEW"
    ) {
        val today = dateFmt.format(Date())
        startActivity(
            Intent(this, MarkAttendanceActivity::class.java).apply {
                putExtra("btCode",         btCode)
                putExtra("companyCode",    companyCode)
                putExtra("companyName",    companyName)
                putExtra("shiftCode",      shift.shiftCode)
                putExtra("shiftName",      shift.shiftName)
                putExtra("shiftStartTime", shift.startTime)
                putExtra("shiftEndTime",   shift.endTime)
                putExtra("date",           today)
                putExtra("mode",           mode)
                attendanceId?.let { putExtra("attendanceId", it) }
            }
        )
    }

    // ─────────────────────────────────
    // ✅ Refresh after returning from MarkAttendance
    // ─────────────────────────────────
    override fun onResume() {
        super.onResume()
        if (btCode.isNotEmpty() && companyCode.isNotEmpty()) {
            viewModel.loadTodayStatus(btCode, companyCode)
        }
    }
}