package com.blivtech.emptrack.ui.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.model.AttendanceEmployeeItem
import com.blivtech.emptrack.data.repository.ReportRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShiftEmployeeListViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _employees = MutableLiveData<List<AttendanceEmployeeItem>>()
    val employees: LiveData<List<AttendanceEmployeeItem>> = _employees

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // ✅ Load from API
    fun loadEmployees(
        btCode: String,
        companyCode: String,
        date: String,
        shiftCode: String,
        type: String
    ) {
        viewModelScope.launch {
            reportRepository.getEmployeesByShiftAndType(
                btCode, companyCode, date, shiftCode, type
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _loading.value = true
                    is Resource.Success -> {
                        _loading.value = false
                        _employees.value = resource.data
                    }
                    is Resource.Error -> {
                        _loading.value = false
                        _error.value = resource.message
                    }
                }
            }
        }
    }
}