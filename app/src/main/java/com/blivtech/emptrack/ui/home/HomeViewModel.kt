package com.blivtech.emptrack.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.local.entity.CompanyEntity
import com.blivtech.emptrack.data.local.entity.ShiftEntity
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

    fun getCompanies(btCode: String) =
        masterRepository.getCompanies(btCode).asLiveData()

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
}

data class SyncResult(
    val companiesCount: Int,
    val shiftsCount: Int,
    val departmentsCount: Int
)