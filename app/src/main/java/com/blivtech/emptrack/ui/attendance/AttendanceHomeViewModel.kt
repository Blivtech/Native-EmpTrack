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
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AttendanceHomeViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val companyRepository: CompanyRepository,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    private val dateFmt    = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFmt = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())

    // ─────────────────────────────────
    // ✅ Selected date — defaults to today
    // ─────────────────────────────────
    private val _selectedDate = MutableLiveData<String>().apply {
        value = dateFmt.format(Date())
    }
    val selectedDate: LiveData<String> = _selectedDate

    private val _selectedDateLabel = MutableLiveData<String>().apply {
        value = displayFmt.format(Date())
    }
    val selectedDateLabel: LiveData<String> = _selectedDateLabel

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

    // ─────────────────────────────────
    // ✅ Set selected date — called from DatePickerDialog callback
    // ─────────────────────────────────
    fun setSelectedDate(year: Int, month: Int, day: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }
        _selectedDate.value      = dateFmt.format(cal.time)
        _selectedDateLabel.value = displayFmt.format(cal.time)
    }

    fun setToday() {
        val now = Date()
        _selectedDate.value      = dateFmt.format(now)
        _selectedDateLabel.value = displayFmt.format(now)
    }

    // ✅ Used to pre-fill the DatePickerDialog with the currently selected date
    fun getSelectedCalendar(): Calendar {
        val cal = Calendar.getInstance()
        try {
            cal.time = dateFmt.parse(_selectedDate.value ?: "") ?: Date()
        } catch (e: Exception) { }
        return cal
    }

    fun isToday(): Boolean =
        _selectedDate.value == dateFmt.format(Date())

    // ─────────────────────────────────
    // ✅ Load attendance status for the SELECTED date
    // ─────────────────────────────────
    fun loadTodayStatus(btCode: String, companyCode: String) {
        _todayStatus.value = Resource.Loading
        viewModelScope.launch {
            val date = _selectedDate.value ?: dateFmt.format(Date())
            _todayStatus.value = attendanceRepository
                .getTodayStatus(btCode, companyCode, date)
        }
    }

    // ✅ Returns the SELECTED date (used by openMarkAttendance)
    fun getTodayDate(): String = _selectedDate.value ?: dateFmt.format(Date())
}