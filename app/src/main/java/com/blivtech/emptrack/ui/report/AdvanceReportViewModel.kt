// AdvanceReportViewModel.kt
package com.blivtech.emptrack.ui.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.model.AdvanceEntryDto
import com.blivtech.emptrack.data.model.AdvanceMonthlyDto
import com.blivtech.emptrack.data.repository.WageReportRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AdvanceReportViewModel @Inject constructor(
    private val repository: WageReportRepository
) : ViewModel() {

    private val apiSdf   = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val labelSdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    private val _currentMonth = MutableLiveData(apiSdf.format(Date()))
    private val _monthLabel   = MutableLiveData(labelSdf.format(Date()))
    val monthLabel: LiveData<String> = _monthLabel

    private val _monthlyData = MutableLiveData<AdvanceMonthlyDto?>()
    val monthlyData: LiveData<AdvanceMonthlyDto?> = _monthlyData

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _actionState = MutableLiveData<Resource<*>?>()
    val actionState: LiveData<Resource<*>?> = _actionState

    fun prevMonth() {
        val cal = Calendar.getInstance().apply {
            time = apiSdf.parse(_currentMonth.value!!)!!
        }
        cal.add(Calendar.MONTH, -1)
        _currentMonth.value = apiSdf.format(cal.time)
        _monthLabel.value   = labelSdf.format(cal.time)
    }

    fun nextMonth() {
        val cal = Calendar.getInstance().apply {
            time = apiSdf.parse(_currentMonth.value!!)!!
        }
        cal.add(Calendar.MONTH, 1)
        if (cal.time.after(Date())) return
        _currentMonth.value = apiSdf.format(cal.time)
        _monthLabel.value   = labelSdf.format(cal.time)
    }

    fun loadAdvanceList(btCode: String, companyCode: String) {
        val month = _currentMonth.value ?: return
        viewModelScope.launch {
            repository.getAdvanceList(btCode, companyCode, month).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _loading.value = true
                    is Resource.Success -> {
                        _loading.value  = false
                        _monthlyData.value = resource.data
                    }
                    is Resource.Error -> {
                        _loading.value = false
                        _error.value   = resource.message
                    }
                }
            }
        }
    }

    fun updateAdvance(
        entry: AdvanceEntryDto,
        requestDate: String,
        amount: Double,
        repayMonth: String,
        remarks: String
    ) {
        viewModelScope.launch {
            _actionState.value = repository.updateAdvance(
                advanceId   = entry.advanceId,
                btCode      = entry.btCode,
                companyCode = entry.companyCode,
                empCode     = entry.empCode,
                requestDate = requestDate,
                amount      = amount,
                repayMonth  = repayMonth,
                remarks     = remarks
            )
        }
    }

    fun deleteAdvance(entry: AdvanceEntryDto) {
        viewModelScope.launch {
            _actionState.value = repository.deleteAdvance(
                advanceId   = entry.advanceId,
                btCode      = entry.btCode,
                companyCode = entry.companyCode
            )
        }
    }

    fun resetActionState() { _actionState.value = null }
    fun resetError()       { _error.value = null }
}