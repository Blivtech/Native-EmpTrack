package com.blivtech.emptrack.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.local.entity.ProductWithWorks
import com.blivtech.emptrack.data.model.ProductRequestDto
import com.blivtech.emptrack.data.repository.ProductRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private var btCode = ""
    private val companyCode = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<ProductWithWorks>> =
        companyCode.filterNotNull()
            .flatMapLatest { repository.observeProducts(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _editTarget = MutableStateFlow<ProductWithWorks?>(null)
    val editTarget: StateFlow<ProductWithWorks?> = _editTarget.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun setScope(btCode: String, companyCode: String) {
        this.btCode = btCode
        this.companyCode.value = companyCode
    }

    fun startAdd() { _editTarget.value = null }
    fun startEdit(product: ProductWithWorks) { _editTarget.value = product }

    fun save(editingId: Long?, body: ProductRequestDto) {
        val cc = companyCode.value ?: return
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            val result = if (editingId == null) {
                repository.createProduct(btCode, cc, body)
            } else {
                repository.updateProduct(btCode, cc, editingId, body)
            }
            _saveState.value = result.toSaveState()
        }
    }

    fun delete(id: Long) {
        val cc = companyCode.value ?: return
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            _saveState.value = repository.deleteProduct(btCode, cc, id).toSaveState()
        }
    }

    fun consumeSaveState() { _saveState.value = SaveState.Idle }

    private fun Resource<Unit>.toSaveState(): SaveState = when (this) {
        is Resource.Success -> SaveState.Done
        is Resource.Error -> SaveState.Error(message ?: "Something went wrong")
        Resource.Loading -> SaveState.Loading
    }

    sealed interface SaveState {
        data object Idle : SaveState
        data object Loading : SaveState
        data object Done : SaveState
        data class Error(val message: String) : SaveState
    }
}