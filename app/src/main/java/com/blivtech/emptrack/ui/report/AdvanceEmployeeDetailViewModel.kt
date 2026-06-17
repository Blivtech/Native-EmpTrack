package com.blivtech.emptrack.ui.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.model.AdvanceEmployeeDetail
import com.blivtech.emptrack.data.repository.AdvanceRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdvanceEmployeeDetailViewModel @Inject constructor(
    private val repository: AdvanceRepository
) : ViewModel() {

    private val _detail  = MutableLiveData<AdvanceEmployeeDetail?>()
    val detail: LiveData<AdvanceEmployeeDetail?> = _detail

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error   = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadDetail(
        btCode: String,
        companyCode: String,
        month: String,
        empCode: String
    ) {
        viewModelScope.launch {
            repository.getAdvanceEmployeeDetail(
                btCode, companyCode, month, empCode
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _loading.value = true
                    is Resource.Success -> {
                        _loading.value = false
                        _detail.value  = resource.data
                    }
                    is Resource.Error -> {
                        _loading.value = false
                        _error.value   = resource.message
                    }
                }
            }
        }
    }

    fun formatAmount(amount: Double): String =
        "₹${String.format("%,.0f", amount)}"

    fun resetError() { _error.value = null }
}