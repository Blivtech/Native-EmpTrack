package com.blivtech.emptrack.ui.attendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.model.ShiftStatusResponse
import com.blivtech.emptrack.data.repository.AttendanceRepository
import com.blivtech.emptrack.data.repository.CompanyRepository
import com.blivtech.emptrack.data.repository.EmployeeRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttendanceHomeViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val companyRepository: CompanyRepository,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    private val _todayStatus = MutableLiveData<Resource<List<ShiftStatusResponse>>>()
    val todayStatus: LiveData<Resource<List<ShiftStatusResponse>>> = _todayStatus

    private val _employeeCount = MutableLiveData<Int>()
    val employeeCount: LiveData<Int> = _employeeCount

    // ✅ Get companies from Room
    fun getCompanies(btCode: String) =
        companyRepository.getCompanies(btCode).asLiveData()

    // ✅ Get shifts from Room
    fun getShifts(companyCode: String) =
        companyRepository.getShiftsByCompany(companyCode).asLiveData()

    // ✅ Load employee count
    fun loadEmployeeCount(companyId: String) {
        viewModelScope.launch {
            employeeRepository.getEmployees(companyId).collect { list ->
                _employeeCount.value = list.size
            }
        }
    }

    // ✅ Load today's shift status
    fun loadTodayStatus(btCode: String, companyId: String) {
        _todayStatus.value = Resource.Loading
        viewModelScope.launch {
            _todayStatus.value =
                attendanceRepository.getTodayStatus(btCode, companyId)
        }
    }
}