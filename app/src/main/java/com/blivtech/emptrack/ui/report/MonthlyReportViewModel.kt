package com.blivtech.emptrack.ui.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.local.entity.ShiftEntity
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
class MonthlyReportViewModel @Inject constructor(
    private val repository: ReportRepository,
    private val companyRepository: CompanyRepository      // ✅ Add this
) : ViewModel() {

    // ─────────────────────────────────
    // ✅ Month state
    // ─────────────────────────────────
    private val _currentMonth = MutableLiveData<String>()
    val currentMonth: LiveData<String> = _currentMonth

    private val _monthLabel = MutableLiveData<String>()
    val monthLabel: LiveData<String> = _monthLabel

    // ─────────────────────────────────
    // ✅ Overall report
    // ─────────────────────────────────
    private val _overallReport = MutableLiveData<MonthlyReportDto?>()
    val overallReport: LiveData<MonthlyReportDto?> = _overallReport

    // ─────────────────────────────────
    // ✅ Shift wise report
    // ─────────────────────────────────
    private val _shiftReport = MutableLiveData<MonthlyShiftReportDto?>()
    val shiftReport: LiveData<MonthlyShiftReportDto?> = _shiftReport

    // ─────────────────────────────────
    // ✅ Employee detail
    // ─────────────────────────────────
    private val _employeeDetail = MutableLiveData<MonthlyEmployeeDetail?>()
    val employeeDetail: LiveData<MonthlyEmployeeDetail?> = _employeeDetail

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
    init {
        setCurrentMonth()
    }

    private fun setCurrentMonth() {
        val cal = Calendar.getInstance()
        updateMonth(cal)
    }

    // ─────────────────────────────────
    // ✅ Month navigation
    // ─────────────────────────────────
    fun prevMonth() {
        val cal = calFromMonth(_currentMonth.value ?: "")
        cal.add(Calendar.MONTH, -1)
        updateMonth(cal)
    }

    fun nextMonth() {
        val cal = calFromMonth(_currentMonth.value ?: "")
        cal.add(Calendar.MONTH, 1)
        // ✅ No future months
        if (cal.time.after(Date())) return
        updateMonth(cal)
    }

    private fun updateMonth(cal: Calendar) {
        val apiSdf   = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val labelSdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        _currentMonth.value = apiSdf.format(cal.time)
        _monthLabel.value   = labelSdf.format(cal.time)
    }

    private fun calFromMonth(month: String): Calendar {
        val cal = Calendar.getInstance()
        try {
            cal.time = SimpleDateFormat(
                "yyyy-MM", Locale.getDefault()
            ).parse(month) ?: Date()
        } catch (e: Exception) { }
        return cal
    }
    fun getShifts(companyCode: String): LiveData<List<ShiftEntity>> =
        companyRepository.getShiftsByCompany(companyCode).asLiveData()
    // ─────────────────────────────────
    // ✅ Load overall report
    // ─────────────────────────────────
    fun loadOverallReport(btCode: String, companyCode: String) {
        val month = _currentMonth.value ?: return
        viewModelScope.launch {
            repository.getMonthlyReport(btCode, companyCode, month)
                .collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _loading.value = true
                        is Resource.Success -> {
                            _loading.value   = false
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
    // ✅ Load shift wise report
    // ─────────────────────────────────
    fun loadShiftReport(
        btCode: String, companyCode: String, shiftCode: String
    ) {
        val month = _currentMonth.value ?: return
        viewModelScope.launch {
            repository.getMonthlyShiftReport(
                btCode, companyCode, month, shiftCode
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _loading.value = true
                    is Resource.Success -> {
                        _loading.value     = false
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
    // ✅ Load employee detail
    // ─────────────────────────────────
    fun loadEmployeeDetail(
        btCode: String, companyCode: String,
        shiftCode: String, empCode: String
    ) {
        val month = _currentMonth.value ?: return
        viewModelScope.launch {
            repository.getMonthlyEmployeeDetail(
                btCode, companyCode, month, shiftCode, empCode
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _loading.value = true
                    is Resource.Success -> {
                        _loading.value        = false
                        _employeeDetail.value = resource.data
                    }
                    is Resource.Error -> {
                        _loading.value = false
                        _error.value   = resource.message
                    }
                }
            }
        }
    }

    fun resetError() { _error.value = null }
}