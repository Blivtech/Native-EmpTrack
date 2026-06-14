package com.blivtech.emptrack.ui.company

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.local.entity.CompanyEntity
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.data.repository.CompanyRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyListViewModel @Inject constructor(
    private val repository: CompanyRepository
) : ViewModel() {

    private val _deleteState = MutableLiveData<Resource<Any>>()
    val deleteState: LiveData<Resource<Any>> = _deleteState

    private val _selectedCompany = MutableLiveData<CompanyEntity?>()
    val selectedCompany: LiveData<CompanyEntity?> = _selectedCompany

    // ✅ Selected company id persisted
    private var selectedCompanyId: Long = -1L

    fun getCompanies() =
        repository.getCompanies().asLiveData()

    fun getShifts(companyCode: String) =
        repository.getShiftsByCompany(companyCode).asLiveData()

    fun selectCompany(company: CompanyEntity) {
        selectedCompanyId = company.id
        _selectedCompany.value = company
    }

    fun getSelectedCompanyId() = selectedCompanyId

    fun deleteCompany(company: CompanyEntity, btCode: String) {
        _deleteState.value = Resource.Loading
        viewModelScope.launch {
            _deleteState.value = repository.deleteCompany(company.id, btCode)
        }
    }
}