package com.blivtech.emptrack.ui.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.model.AdvanceReportDto
import com.blivtech.emptrack.data.repository.AdvanceRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AdvanceReportViewModel @Inject constructor(
    private val repository: AdvanceRepository
) : ViewModel() {

    // ─────────────────────────────────
    // ✅ Month state
    // ─────────────────────────────────
    private val _currentMonth = MutableLiveData<String>()
    val currentMonth: LiveData<String> = _currentMonth

    private val _monthLabel = MutableLiveData<String>()
    val monthLabel: LiveData<String> = _monthLabel

    private val _monthSubLabel = MutableLiveData<String>()
    val monthSubLabel: LiveData<String> = _monthSubLabel

    // ─────────────────────────────────
    // ✅ Report data
    // ─────────────────────────────────
    private val _report  = MutableLiveData<AdvanceReportDto?>()
    val report: LiveData<AdvanceReportDto?> = _report

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error   = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // ─────────────────────────────────
    // ✅ Init
    // ─────────────────────────────────
    init { setCurrentMonth() }

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
        if (cal.time.after(Date())) return
        updateMonth(cal)
    }

    private fun updateMonth(cal: Calendar) {
        val apiSdf   = SimpleDateFormat("yyyy-MM",   Locale.getDefault())
        val labelSdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val subSdf   = SimpleDateFormat("MMM yyyy",  Locale.getDefault())
        _currentMonth.value  = apiSdf.format(cal.time)
        _monthLabel.value    = labelSdf.format(cal.time)
        _monthSubLabel.value = subSdf.format(cal.time)
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

    // ─────────────────────────────────
    // ✅ Load advance report
    // ─────────────────────────────────
    fun loadReport(btCode: String, companyCode: String) {
        val month = _currentMonth.value ?: return
        viewModelScope.launch {
            repository.getAdvanceReport(btCode, companyCode, month)
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
    // ✅ Format amount
    // ─────────────────────────────────
    fun formatAmount(amount: Double): String {
        return "₹${String.format("%,.0f", amount)}"
    }

    fun resetError() { _error.value = null }
}