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
import com.blivtech.emptrack.data.model.DashboardItem
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
        CardDetailsForHomeActivity(id = "1", iconRes = R.drawable.ic_nav_reports, bgColorRes = R.drawable.bg_circle_red, cardName = "Advance", subtitle = "advance,overtime,bonus", needFlag = true),
        CardDetailsForHomeActivity(id = "2", iconRes = R.drawable.ic_nav_attendance, bgColorRes = R.drawable.bg_circle_blue, cardName = "Shift Mgmt", subtitle = "Pre Plan Shift", needFlag = false),
        CardDetailsForHomeActivity(id = "3", iconRes = R.drawable.ic_nav_work, bgColorRes = R.drawable.bg_circle_amber, cardName = "Work Progress", subtitle = "Task · Entry · Status", needFlag = false),
        CardDetailsForHomeActivity(id = "4", iconRes = R.drawable.ic_nav_reports, bgColorRes = R.drawable.bg_circle_blue, cardName = "Salary", subtitle = "Work based · Pay", needFlag = false),
        CardDetailsForHomeActivity(id = "5", iconRes = R.drawable.ic_nav_work, bgColorRes = R.drawable.bg_circle_green, cardName = "Inventory", subtitle = "Stock · Issue · Track", needFlag = false),
    )


    val items = listOf(
        DashboardItem("Extra Pay & Advances", R.drawable.ic_extra_pay,    R.color.mod_extra_ic,    R.color.mod_extra_bg),
        DashboardItem("Shift Management",     R.drawable.ic_shift,     R.color.mod_shift_ic,    R.color.mod_shift_bg),
        DashboardItem("Work-Based Pay",       R.drawable.ic_work,      R.color.mod_work_ic,     R.color.mod_work_bg),
        DashboardItem("Salary",               R.drawable.ic_salary,    R.color.mod_salary_ic,   R.color.mod_salary_bg),
        DashboardItem("Product",            R.drawable.bg_product, R.color.mod_inv_ic,      R.color.mod_inv_bg),
        DashboardItem("Contractor Billing",   R.drawable.ic_billing,  R.color.mod_contract_ic, R.color.mod_contract_bg)
    )


}

data class SyncResult(
    val companiesCount: Int,
    val shiftsCount: Int,
    val departmentsCount: Int
)


