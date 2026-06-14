package com.blivtech.emptrack.ui.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.model.ShiftAttendanceSummary
import com.blivtech.emptrack.data.repository.ReportRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DailyReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    // ─────────────────────────────────
    // ✅ Selected date
    // ─────────────────────────────────
    private val _selectedDate = MutableLiveData(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val selectedDate: LiveData<String> = _selectedDate

    // ─────────────────────────────────
    // ✅ Display date
    // ─────────────────────────────────
    private val _displayDate = MutableLiveData(
        SimpleDateFormat("EEEE, d MMM yyyy", Locale.getDefault()).format(Date())
    )
    val displayDate: LiveData<String> = _displayDate

    // ─────────────────────────────────
    // ✅ Loading state
    // ─────────────────────────────────
    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    // ─────────────────────────────────
    // ✅ Error state
    // ─────────────────────────────────
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // ─────────────────────────────────
    // ✅ Shift summaries
    // ─────────────────────────────────
    private val _summaries = MutableLiveData<List<ShiftAttendanceSummary>>()
    val summaries: LiveData<List<ShiftAttendanceSummary>> = _summaries

    // ─────────────────────────────────
    // ✅ Overall totals (computed)
    // ─────────────────────────────────
    val totalPresent: Int get() =
        _summaries.value?.sumOf { it.presentCount } ?: 0

    val totalLeave: Int get() =
        _summaries.value?.sumOf { it.leaveCount } ?: 0

    val totalEmployees: Int get() =
        _summaries.value?.sumOf { it.totalCount } ?: 0

    // ─────────────────────────────────
    // ✅ Date navigation
    // ─────────────────────────────────
    fun prevDay() {
        val cal = calFromDate(_selectedDate.value ?: "")
        cal.add(Calendar.DAY_OF_MONTH, -1)
        setDate(cal)
    }

    fun nextDay() {
        val cal = calFromDate(_selectedDate.value ?: "")
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val today = Calendar.getInstance()
        if (cal.after(today)) return    // ✅ No future dates
        setDate(cal)
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        _displayDate.value = try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val out = SimpleDateFormat("EEEE, d MMM yyyy", Locale.getDefault())
            out.format(sdf.parse(date) ?: Date())
        } catch (e: Exception) { date }
    }

    private fun setDate(cal: Calendar) {
        _selectedDate.value = SimpleDateFormat(
            "yyyy-MM-dd", Locale.getDefault()
        ).format(cal.time)
        _displayDate.value = SimpleDateFormat(
            "EEEE, d MMM yyyy", Locale.getDefault()
        ).format(cal.time)
    }

    private fun calFromDate(date: String): Calendar {
        val cal = Calendar.getInstance()
        try {
            cal.time = SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault()
            ).parse(date) ?: Date()
        } catch (e: Exception) { }
        return cal
    }

    // ─────────────────────────────────
    // ✅ Load summaries from API
    // ─────────────────────────────────
    fun loadSummaries(btCode: String, companyCode: String) {
        viewModelScope.launch {
            val date = _selectedDate.value ?: return@launch
            reportRepository.getShiftSummaries(btCode, companyCode, date)
                .collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _loading.value = true
                        }
                        is Resource.Success -> {
                            _loading.value = false
                            _summaries.value = resource.data
                        }
                        is Resource.Error -> {
                            _loading.value = false
                            _error.value = resource.message
                        }
                    }
                }
        }
    }

    // ─────────────────────────────────
    // ✅ Shift emoji helper
    // ─────────────────────────────────
    fun shiftEmoji(shiftName: String): String {
        return when {
            shiftName.contains("morning", true) ||
                    shiftName.contains("day",     true) -> "☀️"
            shiftName.contains("evening", true) -> "🌆"
            shiftName.contains("night",   true) -> "🌙"
            else                                -> "🕐"
        }
    }

    // ─────────────────────────────────
    // ✅ Reset error
    // ─────────────────────────────────
    fun resetError() {
        _error.value = null
    }
}