// OvertimeReportViewModel.kt
package com.blivtech.emptrack.ui.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.model.OvertimeEntryDto
import com.blivtech.emptrack.data.model.OvertimeMonthlyDto
import com.blivtech.emptrack.data.repository.WageReportRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class OvertimeReportViewModel @Inject constructor(
    private val repository: WageReportRepository
) : ViewModel() {

    private val apiSdf   = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val labelSdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    private val _currentMonth = MutableLiveData(apiSdf.format(Date()))
    private val _monthLabel   = MutableLiveData(labelSdf.format(Date()))
    val monthLabel: LiveData<String> = _monthLabel

    private val _monthlyData = MutableLiveData<OvertimeMonthlyDto?>()
    val monthlyData: LiveData<OvertimeMonthlyDto?> = _monthlyData

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

    fun loadOvertimeList(btCode: String, companyCode: String) {
        val month = _currentMonth.value ?: return
        viewModelScope.launch {
            repository.getOvertimeList(btCode, companyCode, month).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _loading.value = true
                    is Resource.Success -> {
                        _loading.value     = false
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

    fun updateOvertime(
        entry: OvertimeEntryDto,
        otDate: String,
        otHours: Double,
        otAmount: Double,
        remarks: String
    ) {
        viewModelScope.launch {
            _actionState.value = repository.updateOvertime(
                otId        = entry.otId,
                btCode      = entry.btCode,
                companyCode = entry.companyCode,
                empCode     = entry.empCode,
                otDate      = otDate,
                otHours     = otHours,
                otAmount    = otAmount,
                remarks     = remarks
            )
        }
    }

    fun deleteOvertime(entry: OvertimeEntryDto) {
        viewModelScope.launch {
            _actionState.value = repository.deleteOvertime(
                otId        = entry.otId,
                btCode      = entry.btCode,
                companyCode = entry.companyCode
            )
        }
    }

    fun resetActionState() { _actionState.value = null }
    fun resetError()       { _error.value = null }
}