package com.blivtech.emptrack.ui.attendance

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.CompanyEntity
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.data.model.ShiftStatusResponse
import com.blivtech.emptrack.databinding.ActivityAttendanceHomeBinding
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AttendanceHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceHomeBinding
    private val viewModel: AttendanceHomeViewModel by viewModels()

    private val btCode by lazy { intent.getStringExtra("btCode") ?: "" }
    private val companyName by lazy { intent.getStringExtra("companyName") ?: "" }
    private val companyCode by lazy { intent.getStringExtra("companyCode" ) ?:""}

    @SuppressLint("NewApi")
    private val today = LocalDate.now()
    @SuppressLint("NewApi")
    private val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

    private var shifts = listOf<ShiftEntity>()
    private var shiftStatusList = listOf<ShiftStatusResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeData()

        viewModel.loadEmployeeCount(companyCode)
        viewModel.loadTodayStatus(btCode, companyCode)
    }

    override fun onResume() {
        super.onResume()
        // ✅ Refresh on return from mark attendance
        viewModel.loadTodayStatus(btCode, companyCode)
    }

    @SuppressLint("NewApi")
    private fun setupUI() {
        binding.ivBack.setOnClickListener { finish() }
        binding.tvCompanyName.text = companyName
        binding.tvDate.text = today.format(
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy")
        )
    }

    private fun observeData() {

        // ✅ Employee count
        viewModel.employeeCount.observe(this) { count ->
            binding.tvTotalEmp.text = count.toString()
        }

        // ✅ Load shifts from Room
        viewModel.getShifts(companyCode).observe(this) { shiftList ->
            shifts = shiftList
            setupShiftCards()
        }

        // ✅ Today's attendance status
        viewModel.todayStatus.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    shiftStatusList = resource.data
                    updateShiftCards()
                    updateStats()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun setupShiftCards() {
        // ✅ Show/hide cards based on shift count
        binding.cardShift1.visibility =
            if (shifts.isNotEmpty()) View.VISIBLE else View.GONE
        binding.cardShift2.visibility =
            if (shifts.size > 1) View.VISIBLE else View.GONE
        binding.cardShift3.visibility =
            if (shifts.size > 2) View.VISIBLE else View.GONE

        shifts.forEachIndexed { index, shift ->
            when (index) {
                0 -> {
                    binding.tvShift1Name.text = "Shift 1 · ${shift.shiftName}"
                    binding.tvShift1Time.text =
                        "${shift.startTime.take(5)} – ${shift.endTime.take(5)}"
                }
                1 -> {
                    binding.tvShift2Name.text = "Shift 2 · ${shift.shiftName}"
                    binding.tvShift2Time.text =
                        "${shift.startTime.take(5)} – ${shift.endTime.take(5)}"
                }
                2 -> {
                    binding.tvShift3Name.text = "Shift 3 · ${shift.shiftName}"
                    binding.tvShift3Time.text =
                        "${shift.startTime.take(5)} – ${shift.endTime.take(5)}"
                }
            }
        }
    }

    // ✅ Change CardView to MaterialCardView in updateShiftCards()
    private fun updateShiftCards() {
        shifts.forEachIndexed { index, shift ->
            val status = shiftStatusList.find { it.shiftCode == shift.shiftCode }
            val isMarked = status?.isMarked == true

            when (index) {
                0 -> updateCard(
                    isMarked    = isMarked,
                    status      = status,
                    nameView    = binding.tvShift1Name,
                    statusBadge = binding.tvShift1Badge,
                    countView   = binding.tvShift1Count,
                    progView    = binding.progShift1,
                    btnView     = binding.btnShift1,
                    shift       = shift,
                    cardView    = binding.cardShift1  // ✅ Now MaterialCardView
                )
                1 -> updateCard(
                    isMarked    = isMarked,
                    status      = status,
                    nameView    = binding.tvShift2Name,
                    statusBadge = binding.tvShift2Badge,
                    countView   = binding.tvShift2Count,
                    progView    = binding.progShift2,
                    btnView     = binding.btnShift2,
                    shift       = shift,
                    cardView    = binding.cardShift2  // ✅ Now MaterialCardView
                )
                2 -> updateCard(
                    isMarked    = isMarked,
                    status      = status,
                    nameView    = binding.tvShift3Name,
                    statusBadge = binding.tvShift3Badge,
                    countView   = binding.tvShift3Count,
                    progView    = binding.progShift3,
                    btnView     = binding.btnShift3,
                    shift       = shift,
                    cardView    = binding.cardShift3  // ✅ Now MaterialCardView
                )
            }
        }
    }

    private fun updateCard(
        isMarked: Boolean,
        status: ShiftStatusResponse?,
        nameView: android.widget.TextView,
        statusBadge: android.widget.TextView,
        countView: android.widget.TextView,
        progView: android.widget.ProgressBar,
        btnView: android.widget.Button,
        shift: ShiftEntity,
        cardView: com.google.android.material.card.MaterialCardView  // ✅ MaterialCardView
    ) {
        val total = binding.tvTotalEmp.text.toString().toIntOrNull() ?: 0

        if (isMarked && status != null) {
            // ✅ Done state
            statusBadge.text = "Done ✓"
            statusBadge.setBackgroundResource(
                com.blivtech.emptrack.R.drawable.bg_badge_green
            )
            countView.text =
                "✅ ${status.presentCount.toInt()} present · ❌ ${status.absentCount} absent"
            val prog = if (total > 0)
                ((status.presentCount / total) * 100).toInt() else 0
            progView.progress = prog
            btnView.text = "Edit"
            btnView.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    getColor(android.R.color.holo_green_light)
                )

            // ✅ Fix: use strokeColor on MaterialCardView
            cardView.strokeColor = getColor(R.color.green_stroke)
            cardView.strokeWidth = 2

            btnView.setOnClickListener {
                openMarkAttendance(
                    shift        = shift,
                    mode         = "EDIT",
                    attendanceId = status.attendanceId
                )
            }

        } else {
            // ✅ Not marked state
            statusBadge.text = "Not marked"
            statusBadge.setBackgroundResource(
                com.blivtech.emptrack.R.drawable.bg_badge_blue
            )
            countView.text = "Tap to mark attendance"
            progView.progress = 0
            btnView.text = "Mark now"
            btnView.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    getColor(android.R.color.holo_blue_dark)
                )

            // ✅ Reset stroke
            cardView.strokeColor = getColor(android.R.color.transparent)
            cardView.strokeWidth = 0

            btnView.setOnClickListener {
                openMarkAttendance(
                    shift        = shift,
                    mode         = "NEW",
                    attendanceId = null
                )
            }
        }
    }

    private fun updateStats() {
        var totalPresent = 0.0
        var totalAbsent = 0
        var totalOffLeave = 0

        shiftStatusList.forEach { s ->
            totalPresent  += s.presentCount
            totalAbsent   += s.absentCount
            totalOffLeave += s.weekoffCount + s.leaveCount + s.holidayCount
        }

        binding.tvPresent.text = totalPresent.toInt().toString()
        binding.tvAbsent.text = totalAbsent.toString()
        binding.tvOffLeave.text = totalOffLeave.toString()
    }

    private fun openMarkAttendance(
        shift: ShiftEntity,
        mode: String,
        attendanceId: String?
    ) {
        startActivity(
            Intent(this, MarkAttendanceActivity::class.java).apply {
                putExtra("btCode", btCode)
                putExtra("companyCode", companyCode)
                putExtra("companyName", companyName)
                putExtra("shiftId", shift.shiftCode)
                putExtra("shiftName", shift.shiftName)
                putExtra("shiftStartTime", shift.startTime)
                putExtra("shiftEndTime", shift.endTime)
                putExtra("date", todayStr)
                putExtra("mode", mode)
                putExtra("attendanceId", attendanceId)
            }
        )
    }
}