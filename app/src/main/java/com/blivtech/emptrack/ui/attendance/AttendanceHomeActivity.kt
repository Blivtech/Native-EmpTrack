package com.blivtech.emptrack.ui.attendance

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.data.model.ShiftStatusResponse
import com.blivtech.emptrack.databinding.ActivityAttendanceHomeBinding
import com.blivtech.emptrack.utils.PreferenceManager
import com.blivtech.emptrack.utils.Resource
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    private var shifts = listOf<ShiftEntity>()
    private var todayStatusList = listOf<ShiftStatusResponse>()
    private val displayFmt = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var btCode = ""
    private var companyCode = ""
    private var companyName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            btCode = preferenceManager.btCode.first()
            companyCode = preferenceManager.selectedCompanyCode.first()
            companyName = preferenceManager.selectedCompanyName.first()

            setupUI()
            observeData()

            viewModel.loadEmpCount(companyCode)
            viewModel.loadTodayStatus(btCode, companyCode)
        }
    }

    // ─────────────────────────────────────
    // ✅ Setup UI
    // ─────────────────────────────────────
    private fun setupUI() {
        binding.ivBack.setOnClickListener { finish() }
        binding.ivCalendar.setOnClickListener {
            // TODO: Open calendar activity
        }
        binding.tvCompanyName.text = companyName
        binding.tvDate.text = displayFmt.format(Date())
    }

    // ─────────────────────────────────────
    // ✅ Observe data
    // ─────────────────────────────────────
    private fun observeData() {

        // ✅ Employee count
        viewModel.empCount.observe(this) { count ->
            binding.tvEmpCount.text = "$count employees"
        }

        // ✅ Load shifts dynamically
        viewModel.getShifts(companyCode).observe(this) { shiftList ->
            shifts = shiftList
            buildShiftCards()
        }

        // ✅ Today's status
        viewModel.todayStatus.observe(this) { resource ->
            binding.progressBar.visibility = View.GONE
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    todayStatusList = resource.data
                    updateShiftCards()
                    updateStatsBar()
                }
                is Resource.Error -> {
                    // ✅ Show default state — not marked
                    updateShiftCards()
                }
            }
        }
    }

    // ─────────────────────────────────────
    // ✅ Build shift cards dynamically
    // ─────────────────────────────────────
    private fun buildShiftCards() {
        binding.layoutShiftCards.removeAllViews()

        if (shifts.isEmpty()) {
            val emptyView = layoutInflater.inflate(
                R.layout.item_empty_state,
                binding.layoutShiftCards,
                false
            )
            binding.layoutShiftCards.addView(emptyView)
            return
        }

        shifts.forEachIndexed { index, shift ->
            val cardView = layoutInflater.inflate(
                R.layout.item_attendance_shift_card,
                binding.layoutShiftCards,
                false
            )

            // ✅ Shift icon color
            val iconLayout = cardView.findViewById<LinearLayout>(R.id.layoutShiftIcon)
            val iconView   = cardView.findViewById<ImageView>(R.id.ivShiftIcon)

            when (index % 3) {
                0 -> {
                    iconLayout.setBackgroundResource(R.drawable.bg_shift_morning)
                    iconView.setColorFilter(
                        android.graphics.Color.parseColor("#27500A")
                    )
                }
                1 -> {
                    iconLayout.setBackgroundResource(R.drawable.bg_shift_evening)
                    iconView.setColorFilter(
                        android.graphics.Color.parseColor("#0C447C")
                    )
                }
                2 -> {
                    iconLayout.setBackgroundResource(R.drawable.bg_shift_night)
                    iconView.setColorFilter(
                        android.graphics.Color.parseColor("#534AB7")
                    )
                }
            }

            // ✅ Shift name + time
            cardView.findViewById<TextView>(R.id.tvShiftName).text =
                "Shift ${index + 1} · ${shift.shiftName}"
            cardView.findViewById<TextView>(R.id.tvShiftTime).text =
                "${shift.startTime.take(5)} – ${shift.endTime.take(5)}"

            // ✅ Default — not marked
            cardView.findViewById<TextView>(R.id.tvShiftBadge).text = "Not marked"
            cardView.findViewById<TextView>(R.id.tvShiftCount).text =
                "Tap to mark attendance"
            cardView.findViewById<ProgressBar>(R.id.progShift).progress = 0

            // ✅ Mark now button
            cardView.findViewById<Button>(R.id.btnMarkAttendance)
                .setOnClickListener {
                    openMarkAttendance(shift)
                }

            binding.layoutShiftCards.addView(cardView)
        }

        // ✅ Update with today's status if available
        if (todayStatusList.isNotEmpty()) updateShiftCards()
    }

    // ─────────────────────────────────────
    // ✅ Update shift cards with today's status
    // ─────────────────────────────────────
    private fun updateShiftCards() {
        shifts.forEachIndexed { index, shift ->
            val cardView = binding.layoutShiftCards
                .getChildAt(index) ?: return@forEachIndexed

            val status = todayStatusList.find {
                it.shiftCode == shift.shiftCode
            }

            val badge  = cardView.findViewById<TextView>(R.id.tvShiftBadge)
            val count  = cardView.findViewById<TextView>(R.id.tvShiftCount)
            val prog   = cardView.findViewById<ProgressBar>(R.id.progShift)
            val btn    = cardView.findViewById<Button>(R.id.btnMarkAttendance)
            val card   = cardView as? MaterialCardView

            if (status != null && status.isMarked) {
                // ✅ Marked — show counts
                val total   = status.presentCount.toInt() +
                        status.absentCount +
                        status.weekoffCount +
                        status.leaveCount +
                        status.holidayCount
                val present = status.presentCount.toInt()
                val pct     = if (total > 0) (present * 100 / total) else 0

                badge.text = "Marked ✓"
                badge.setBackgroundResource(R.drawable.bg_badge_green)
                badge.setTextColor(android.graphics.Color.parseColor("#27500A"))

                count.text = "$present present · ${status.absentCount} absent · ${status.weekoffCount} off"
                count.setTextColor(android.graphics.Color.parseColor("#27500A"))

                prog.progress = pct
                prog.progressDrawable = getDrawable(R.drawable.progress_drawable)

                btn.text = "Edit"
                btn.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#27500A")
                    )
                btn.setOnClickListener {
                    openMarkAttendance(shift, status.attendanceId, "EDIT")
                }

                // ✅ Green stroke for marked card
                card?.strokeColor = android.graphics.Color.parseColor("#639922")
                card?.strokeWidth = 2

            } else {
                // ✅ Not marked
                badge.text = "Not marked"
                badge.setBackgroundResource(R.drawable.bg_badge_blue)
                badge.setTextColor(android.graphics.Color.parseColor("#0C447C"))

                count.text = "Tap to mark attendance"
                count.setTextColor(android.graphics.Color.parseColor("#757575"))

                prog.progress = 0

                btn.text = "Mark now"
                btn.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#1565C0")
                    )
                btn.setOnClickListener {
                    openMarkAttendance(shift)
                }

                card?.strokeColor = android.graphics.Color.parseColor("#E0E0E0")
                card?.strokeWidth = 0
            }
        }
    }

    // ─────────────────────────────────────
    // ✅ Update stats bar
    // ─────────────────────────────────────
    private fun updateStatsBar() {
        var totalPresent = 0.0
        var totalAbsent  = 0
        var totalOffLeave = 0

        todayStatusList.forEach { status ->
            totalPresent  += status.presentCount
            totalAbsent   += status.absentCount
            totalOffLeave += status.weekoffCount + status.leaveCount + status.holidayCount
        }

        binding.tvPresent.text  = totalPresent.toInt().toString()
        binding.tvAbsent.text   = totalAbsent.toString()
        binding.tvOffLeave.text = totalOffLeave.toString()
    }

    // ─────────────────────────────────────
    // ✅ Open mark attendance
    // ─────────────────────────────────────
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
}