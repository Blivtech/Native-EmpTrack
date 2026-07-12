package com.blivtech.emptrack.ui.attendance

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.data.model.ShiftStatusResponse
import com.blivtech.emptrack.databinding.FragmentAttendanceBinding
import com.blivtech.emptrack.utils.PreferenceManager
import com.blivtech.emptrack.utils.Resource
import com.blivtech.emptrack.utils.ShimmerHelper
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class AttendanceHomeFragment : Fragment() {

    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttendanceHomeViewModel by viewModels()

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private lateinit var shiftCardAdapter: ShiftCardAdapter

    private var shifts          = listOf<ShiftEntity>()
    private var todayStatusList = listOf<ShiftStatusResponse>()

    private var btCode      = ""
    private var companyCode = ""
    private var companyName = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
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

    // ✅ NOTE: no ivBack — fragments use bottom nav, not a back button
    private fun setupUI() {
        binding.tvCompanyName.text = companyName
        binding.tvDate.text        = viewModel.selectedDateLabel.value

        // ✅ Calendar tap → open date picker
        binding.ivCalendar.setOnClickListener {
            showDatePicker()
        }
    }

    // ─────────────────────────────────
    // ✅ Date picker — past dates allowed, future dates blocked
    // ─────────────────────────────────
    private fun showDatePicker() {
        val cal = viewModel.getSelectedCalendar()

        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                viewModel.setSelectedDate(year, month, day)
                onDateChanged()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        // ✅ Block future dates — greyed out / unselectable in the picker UI itself
        dialog.datePicker.maxDate = System.currentTimeMillis()

        dialog.show()
    }

    // ─────────────────────────────────
    // ✅ Called whenever the selected date changes
    // ─────────────────────────────────
    private fun onDateChanged() {
        binding.tvDate.text = viewModel.selectedDateLabel.value

        // ✅ Re-fetch attendance status for the newly selected date
        viewModel.loadTodayStatus(btCode, companyCode)
    }

    // ─────────────────────────────────
    // ✅ Setup RecyclerView
    // ─────────────────────────────────
    private fun setupRecyclerView() {
        shiftCardAdapter = ShiftCardAdapter(

            // ✅ Mark now clicked
            onMarkClick = mark@{ item ->
//                if (!viewModel.isToday()) {
//                    Snackbar.make(
//                        binding.root,
//                        "Attendance can only be marked for today",
//                        Snackbar.LENGTH_SHORT
//                    ).show()
//                    return@mark
//                }
                openMarkAttendance(item.shift)
            },

            // ✅ Edit clicked
            onEditClick = edit@{ item ->
//                if (!viewModel.isToday()) {
//                    Snackbar.make(
//                        binding.root,
//                        "Attendance can only be edited for today",
//                        Snackbar.LENGTH_SHORT
//                    ).show()
//                    return@edit
//                }
                openMarkAttendance(
                    shift        = item.shift,
                    attendanceId = item.status?.attendanceId,
                    mode         = "EDIT"
                )
            }
        )
        binding.rvShiftCards.apply {
            adapter       = shiftCardAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
    }

    // ─────────────────────────────────
    // ✅ Observe data
    // ─────────────────────────────────
    private fun observeData() {
        viewModel.empCount.observe(viewLifecycleOwner) { count ->
            binding.tvEmpCount.text = "$count employees"
        }

        viewModel.getShifts(companyCode).observe(viewLifecycleOwner) { shiftList ->
            shifts = shiftList
            submitShiftCards()
        }

        viewModel.todayStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    ShimmerHelper.show(binding.shimmerLayout, binding.layoutMain)
                }
                is Resource.Success -> {
                    ShimmerHelper.hide(binding.shimmerLayout, binding.layoutMain)
                    todayStatusList = resource.data
                    submitShiftCards()
                    // updateStatsBar()
                }
                is Resource.Error -> {
                    ShimmerHelper.hide(binding.shimmerLayout, binding.layoutMain)
                    todayStatusList = emptyList()
                    submitShiftCards()
                }
            }
        }
    }

    // ─────────────────────────────────
    // ✅ Build shift cards
    // ─────────────────────────────────
    private fun submitShiftCards() {
        if (shifts.isEmpty()) {
            binding.layoutEmpty.visibility  = View.VISIBLE
            binding.rvShiftCards.visibility = View.GONE
            return
        }
        binding.layoutEmpty.visibility  = View.GONE
        binding.rvShiftCards.visibility = View.VISIBLE

        val items = shifts.mapIndexed { index, shift ->
            ShiftCardItem(
                shift  = shift,
                index  = index,
                status = todayStatusList.find { it.shiftCode == shift.shiftCode }
            )
        }
        shiftCardAdapter.submitList(items)
    }

    // ─────────────────────────────────
    // ✅ Open mark attendance — uses the SELECTED date, not always today
    // ─────────────────────────────────
    private fun openMarkAttendance(
        shift: ShiftEntity,
        attendanceId: String? = null,
        mode: String = "NEW"
    ) {
        val date = viewModel.getTodayDate()   // ✅ now returns selected date
        startActivity(
            Intent(requireContext(), MarkAttendanceActivity::class.java).apply {
                putExtra("btCode",         btCode)
                putExtra("companyCode",    companyCode)
                putExtra("companyName",    companyName)
                putExtra("shiftCode",      shift.shiftCode)
                putExtra("shiftName",      shift.shiftName)
                putExtra("shiftStartTime", shift.startTime)
                putExtra("shiftEndTime",   shift.endTime)
                putExtra("date",           date)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}