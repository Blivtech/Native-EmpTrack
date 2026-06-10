package com.blivtech.emptrack.ui.work

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.model.*
import com.blivtech.emptrack.data.repository.WorkRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val repository: WorkRepository
) : ViewModel() {

    // ✅ Save state
    private val _saveState = MutableLiveData<Resource<String>>()
    val saveState: LiveData<Resource<String>> = _saveState

    // ✅ Work type list (in memory)
    private val _workTypes = MutableLiveData<MutableList<WorkTypeItem>>(mutableListOf())
    val workTypes: LiveData<MutableList<WorkTypeItem>> = _workTypes

    // ✅ Add work type to list
    fun addWorkType(item: WorkTypeItem) {
        val list = _workTypes.value ?: mutableListOf()
        list.add(item)
        _workTypes.value = list
    }

    // ✅ Remove work type
    fun removeWorkType(index: Int) {
        val list = _workTypes.value ?: return
        if (index < list.size) {
            list.removeAt(index)
            _workTypes.value = list
        }
    }

    // ✅ Update work type
    fun updateWorkType(index: Int, item: WorkTypeItem) {
        val list = _workTypes.value ?: return
        if (index < list.size) {
            list[index] = item
            _workTypes.value = list
        }
    }

    // ✅ Save product
    fun saveProduct(
        btCode: String,
        companyCode: String,
        productName: String,
        description: String?,
        isEdit: Boolean = false,
        productId: String = ""
    ) {
        val wtList = _workTypes.value ?: emptyList()
        val request = ProductRequest(
            btCode      = btCode,
            companyCode = companyCode,
            productName = productName,
            description = description,
            workTypes   = wtList.map { wt ->
                WorkTypeRequest(
                    workTypeName  = wt.name,
                    ratePerPiece  = wt.ratePerPiece,
                    unit          = wt.unit,
                    colorTag      = wt.colorTag
                )
            }
        )

        _saveState.value = Resource.Loading
        viewModelScope.launch {
            _saveState.value = if (isEdit)
                repository.updateProduct(productId, request)
            else
                repository.addProduct(request)
        }
    }
}

// ✅ UI model for work type row
data class WorkTypeItem(
    val id: Int,
    var name: String = "",
    var ratePerPiece: Double = 0.0,
    var unit: String = "pieces",
    var colorTag: String = "#1565C0"
)