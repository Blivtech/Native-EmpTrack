package com.blivtech.emptrack.ui.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.model.WeeklyReportDto
import com.blivtech.emptrack.data.model.WeeklyShiftSummary
import com.blivtech.emptrack.data.repository.ReportRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class WeeklyReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    // ─────────────────────────────────
    // ✅ Week dates
    // ─────────────────────────────────
    private val _weekStart = MutableLiveData<String>()
    val weekStart: LiveData<String> = _weekStart

    private val _weekEnd = MutableLiveData<String>()
    val weekEnd: LiveData<String> = _weekEnd

    private val _weekLabel = MutableLiveData<String>()
    val weekLabel: LiveData<String> = _weekLabel

    private val _weekSubLabel = MutableLiveData<String>()
    val weekSubLabel: LiveData<String> = _weekSubLabel

    // ─────────────────────────────────
    // ✅ Report data
    // ─────────────────────────────────
    private val _report = MutableLiveData<WeeklyReportDto?>()
    val report: LiveData<WeeklyReportDto?> = _report

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // ─────────────────────────────────
    // ✅ Init — set current week
    // ─────────────────────────────────
    init {
        setCurrentWeek()
    }

    private fun setCurrentWeek() {
        val cal = Calendar.getInstance()
        // ✅ Go to Monday of current week
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val start = cal.time
        // ✅ Go to Sunday
        cal.add(Calendar.DAY_OF_WEEK, 6)
        val end = cal.time

        updateWeekDates(start, end)
    }

    // ─────────────────────────────────
    // ✅ Week navigation
    // ─────────────────────────────────
    fun prevWeek() {
        val cal = calFromDate(_weekStart.value ?: "")
        cal.add(Calendar.WEEK_OF_YEAR, -1)
        val start = cal.time
        cal.add(Calendar.DAY_OF_WEEK, 6)
        val end = cal.time
        updateWeekDates(start, end)
    }

    fun nextWeek() {
        val cal = calFromDate(_weekStart.value ?: "")
        cal.add(Calendar.WEEK_OF_YEAR, 1)
        val start = cal.time

        // ✅ No future weeks
        if (start.after(Date())) return

        cal.add(Calendar.DAY_OF_WEEK, 6)
        val end = cal.time
        updateWeekDates(start, end)
    }

    private fun updateWeekDates(start: Date, end: Date) {
        val apiSdf     = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displaySdf = SimpleDateFormat("d MMM", Locale.getDefault())
        val yearSdf    = SimpleDateFormat("yyyy", Locale.getDefault())

        _weekStart.value    = apiSdf.format(start)
        _weekEnd.value      = apiSdf.format(end)

        // ✅ Week number
        val cal = Calendar.getInstance()
        cal.time = start
        val weekNum = cal.get(Calendar.WEEK_OF_MONTH)
        val month   = SimpleDateFormat("MMMM", Locale.getDefault()).format(start)
        val year    = yearSdf.format(start)

        _weekLabel.value    = "Week $weekNum · $month $year"
        _weekSubLabel.value = "${displaySdf.format(start)} – ${displaySdf.format(end)} ${yearSdf.format(end)}"
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
    // ✅ Load weekly report
    // ─────────────────────────────────
    fun loadWeeklyReport(btCode: String, companyCode: String) {
        val start = _weekStart.value ?: return
        val end   = _weekEnd.value   ?: return
        viewModelScope.launch {
            reportRepository.getWeeklySummary(btCode, companyCode, start, end)
                .collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _loading.value = true
                        is Resource.Success -> {
                            _loading.value = false
                            _report.value  = resource.data
                        }
                        is Resource.Error -> {
                            _loading.value = false
                            _error.value   = resource.message
                        }
                    }
                }
        }
    }

    // ─────────────────────────────────
    // ✅ Helpers
    // ─────────────────────────────────
    fun shiftEmoji(shiftName: String): String {
        return when {
            shiftName.contains("morning", true) ||
            shiftName.contains("day", true)     -> "☀️"
            shiftName.contains("evening", true) -> "🌆"
            shiftName.contains("night", true)   -> "🌙"
            else                                -> "🕐"
        }
    }

    fun resetError() { _error.value = null }
}