// BonusReportViewModel.kt
package com.blivtech.emptrack.ui.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.model.BonusEntryDto
import com.blivtech.emptrack.data.model.BonusMonthlyDto
import com.blivtech.emptrack.data.repository.WageReportRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BonusReportViewModel @Inject constructor(
    private val repository: WageReportRepository
) : ViewModel() {

    private val apiSdf   = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val labelSdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    private val _currentMonth = MutableLiveData(apiSdf.format(Date()))
    private val _monthLabel   = MutableLiveData(labelSdf.format(Date()))
    val monthLabel: LiveData<String> = _monthLabel

    private val _monthlyData = MutableLiveData<BonusMonthlyDto?>()
    val monthlyData: LiveData<BonusMonthlyDto?> = _monthlyData

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

    fun loadBonusList(btCode: String, companyCode: String) {
        val month = _currentMonth.value ?: return
        viewModelScope.launch {
            repository.getBonusList(btCode, companyCode, month).collect { resource ->
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

    fun updateBonus(
        entry: BonusEntryDto,
        bonusDate: String,
        bonusType: String,
        amount: Double,
        remarks: String
    ) {
        viewModelScope.launch {
            _actionState.value = repository.updateBonus(
                bonusId     = entry.bonusId,
                btCode      = entry.btCode,
                companyCode = entry.companyCode,
                empCode     = entry.empCode,
                bonusDate   = bonusDate,
                bonusType   = bonusType,
                amount      = amount,
                remarks     = remarks
            )
        }
    }

    fun deleteBonus(entry: BonusEntryDto) {
        viewModelScope.launch {
            _actionState.value = repository.deleteBonus(
                bonusId     = entry.bonusId,
                btCode      = entry.btCode,
                companyCode = entry.companyCode
            )
        }
    }

    fun resetActionState() { _actionState.value = null }
    fun resetError()       { _error.value = null }
}