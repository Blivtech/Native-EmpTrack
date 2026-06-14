package com.blivtech.emptrack.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.CompanyEntity
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.data.model.CardDetailsForHomeActivity
import com.blivtech.emptrack.data.repository.MasterRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val masterRepository: MasterRepository
) : ViewModel() {

    private val _syncState = MutableLiveData<Resource<SyncResult>>()
    val syncState: LiveData<Resource<SyncResult>> = _syncState

    // ✅ Selected company
    private val _selectedCompany = MutableLiveData<CompanyEntity?>()
    val selectedCompany: LiveData<CompanyEntity?> = _selectedCompany

    // ✅ Selected company shifts
    private val _selectedShifts = MutableLiveData<List<ShiftEntity>>()
    val selectedShifts: LiveData<List<ShiftEntity>> = _selectedShifts

    fun syncMasterData(btCode: String) {
        _syncState.value = Resource.Loading
        viewModelScope.launch {
            _syncState.value = masterRepository.syncMasterData(btCode)
        }
    }

    fun getCompanies() =
        masterRepository.getCompanies().asLiveData()

    fun getShifts(companyId: String) =
        masterRepository.getShiftsByCompany(companyId).asLiveData()

    fun getDepartments(btCode: String) =
        masterRepository.getDepartments(btCode).asLiveData()

    // ✅ Called when user selects company from CompanyListActivity
    fun setSelectedCompany(company: CompanyEntity) {
        _selectedCompany.value = company
    }

    fun setSelectedShifts(shifts: List<ShiftEntity>) {
        _selectedShifts.value = shifts
    }



    val moduleCards = listOf(
        CardDetailsForHomeActivity(id = "1", iconRes = R.drawable.ic_nav_attendance, bgColorRes = R.drawable.bg_circle_blue, cardName = "Attendance", subtitle = "Day plan · Shifts", needFlag = true),
        CardDetailsForHomeActivity(id = "2", iconRes = R.drawable.ic_nav_profile, bgColorRes = R.drawable.bg_circle_green, cardName = "Employees", subtitle = "Profile · Role · Team", needFlag = true),
        CardDetailsForHomeActivity(id = "3", iconRes = R.drawable.ic_nav_work, bgColorRes = R.drawable.bg_circle_amber, cardName = "Work Progress", subtitle = "Task · Entry · Status", needFlag = false),
        CardDetailsForHomeActivity(id = "4", iconRes = R.drawable.ic_nav_reports, bgColorRes = R.drawable.bg_circle_blue, cardName = "Salary", subtitle = "Work based · Pay", needFlag = false),
        CardDetailsForHomeActivity(id = "5", iconRes = R.drawable.ic_nav_reports, bgColorRes = R.drawable.bg_circle_red, cardName = "Advance", subtitle = "Request · Approve", needFlag = true),
        CardDetailsForHomeActivity(id = "6", iconRes = R.drawable.ic_nav_work, bgColorRes = R.drawable.bg_circle_green, cardName = "Inventory", subtitle = "Stock · Issue · Track", needFlag = false),
        CardDetailsForHomeActivity(id = "7", iconRes = R.drawable.ic_nav_attendance, bgColorRes = R.drawable.bg_circle_blue, cardName = "Shift Mgmt", subtitle = "Plan · Assign · Track", needFlag = false),
        CardDetailsForHomeActivity(id = "8", iconRes = R.drawable.ic_nav_reports, bgColorRes = R.drawable.bg_circle_white, cardName = "Reports", subtitle = "Summary · Export", needFlag = false)
    )
}

data class SyncResult(
    val companiesCount: Int,
    val shiftsCount: Int,
    val departmentsCount: Int
)


