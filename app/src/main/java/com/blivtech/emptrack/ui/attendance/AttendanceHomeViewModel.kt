package com.blivtech.emptrack.ui.attendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.data.model.ShiftStatusResponse
import com.blivtech.emptrack.data.repository.AttendanceRepository
import com.blivtech.emptrack.data.repository.CompanyRepository
import com.blivtech.emptrack.data.repository.EmployeeRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AttendanceHomeViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val companyRepository: CompanyRepository,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _todayStatus = MutableLiveData<Resource<List<ShiftStatusResponse>>>()
    val todayStatus: LiveData<Resource<List<ShiftStatusResponse>>> = _todayStatus

    private val _empCount = MutableLiveData<Int>()
    val empCount: LiveData<Int> = _empCount

    // ✅ Get shifts for company
    fun getShifts(companyCode: String) =
        companyRepository.getShiftsByCompany(companyCode).asLiveData()

    // ✅ Get employee count
    fun loadEmpCount(companyCode: String) {
        viewModelScope.launch {
            employeeRepository.getEmployees(companyCode)
                .collect { list ->
                    _empCount.value = list.filter { it.status == 1 }.size
                }
        }
    }

    // ✅ Load today's attendance status for all shifts
    fun loadTodayStatus(btCode: String, companyCode: String) {
        _todayStatus.value = Resource.Loading
        viewModelScope.launch {
            val today = dateFmt.format(Date())
            _todayStatus.value = attendanceRepository
                .getTodayStatus(btCode, companyCode)
        }
    }

    // ✅ Get today's date string
    fun getTodayDate(): String = dateFmt.format(Date())
}