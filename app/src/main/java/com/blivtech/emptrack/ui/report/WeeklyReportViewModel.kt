package com.blivtech.emptrack.ui.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.model.*
import com.blivtech.emptrack.data.repository.CompanyRepository
import com.blivtech.emptrack.data.repository.ReportRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class WeeklyReportViewModel @Inject constructor(
    private val repository: ReportRepository,
    private val companyRepository: CompanyRepository
) : ViewModel() {

    // ─────────────────────────────────
    // ✅ Week state
    // ─────────────────────────────────
    private val _weekStart    = MutableLiveData<String>()
    val weekStart: LiveData<String> = _weekStart

    private val _weekEnd      = MutableLiveData<String>()
    val weekEnd: LiveData<String> = _weekEnd

    private val _weekLabel    = MutableLiveData<String>()
    val weekLabel: LiveData<String> = _weekLabel

    private val _weekSubLabel = MutableLiveData<String>()
    val weekSubLabel: LiveData<String> = _weekSubLabel

    // ─────────────────────────────────
    // ✅ Overall report
    // ─────────────────────────────────
    private val _overallReport = MutableLiveData<WeeklyOverallReportDto?>()
    val overallReport: LiveData<WeeklyOverallReportDto?> = _overallReport

    // ─────────────────────────────────
    // ✅ Shift report
    // ─────────────────────────────────
    private val _shiftReport = MutableLiveData<WeeklyShiftReportDto?>()
    val shiftReport: LiveData<WeeklyShiftReportDto?> = _shiftReport

    // ─────────────────────────────────
    // ✅ Loading + Error
    // ─────────────────────────────────
    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // ─────────────────────────────────
    // ✅ Init
    // ─────────────────────────────────
    init { setCurrentWeek() }

    private fun setCurrentWeek() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val start = cal.time
        cal.add(Calendar.DAY_OF_WEEK, 6)
        updateWeekDates(start, cal.time)
    }

    // ─────────────────────────────────
    // ✅ Week navigation
    // ─────────────────────────────────
    fun prevWeek() {
        val cal = calFromDate(_weekStart.value ?: "")
        cal.add(Calendar.WEEK_OF_YEAR, -1)
        val start = cal.time
        cal.add(Calendar.DAY_OF_WEEK, 6)
        updateWeekDates(start, cal.time)
    }

    fun nextWeek() {
        val cal = calFromDate(_weekStart.value ?: "")
        cal.add(Calendar.WEEK_OF_YEAR, 1)
        val start = cal.time
        if (start.after(Date())) return
        cal.add(Calendar.DAY_OF_WEEK, 6)
        updateWeekDates(start, cal.time)
    }

    private fun updateWeekDates(start: Date, end: Date) {
        val apiSdf     = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displaySdf = SimpleDateFormat("d MMM",      Locale.getDefault())
        val yearSdf    = SimpleDateFormat("yyyy",        Locale.getDefault())

        _weekStart.value    = apiSdf.format(start)
        _weekEnd.value      = apiSdf.format(end)

        val cal     = Calendar.getInstance().apply { time = start }
        val weekNum = cal.get(Calendar.WEEK_OF_MONTH)
        val month   = SimpleDateFormat(
            "MMMM", Locale.getDefault()
        ).format(start)

        _weekLabel.value    = "Week $weekNum · $month ${yearSdf.format(start)}"
        _weekSubLabel.value =
            "${displaySdf.format(start)} – ${displaySdf.format(end)} ${yearSdf.format(end)}"
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
    // ✅ Load overall report
    // ─────────────────────────────────
    fun loadOverallReport(btCode: String, companyCode: String) {
        val start = _weekStart.value ?: return
        val end   = _weekEnd.value   ?: return
        viewModelScope.launch {
            repository.getWeeklyOverallReport(
                btCode, companyCode, start, end
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _loading.value = true
                    is Resource.Success -> {
                        _loading.value     = false
                        _overallReport.value = resource.data
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
    // ✅ Load shift report
    // ─────────────────────────────────
    fun loadShiftReport(
        btCode: String, companyCode: String,
        shiftCode: String
    ) {
        val start = _weekStart.value ?: return
        val end   = _weekEnd.value   ?: return
        viewModelScope.launch {
            repository.getWeeklyShiftReport(
                btCode, companyCode, start, end, shiftCode
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _loading.value = true
                    is Resource.Success -> {
                        _loading.value   = false
                        _shiftReport.value = resource.data
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
    fun shiftEmoji(name: String) = when {
        name.contains("morning", true) ||
                name.contains("day",     true) -> "☀️"
        name.contains("evening", true) -> "🌆"
        name.contains("night",   true) -> "🌙"
        else                           -> "🕐"
    }

    fun resetError() { _error.value = null }

    fun getShifts(companyCode: String) =
        companyRepository.getShiftsByCompany(companyCode)
            .asLiveData()
}