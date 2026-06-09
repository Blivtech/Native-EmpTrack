package com.blivtech.emptrack.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.model.RegisterRequest
import com.blivtech.emptrack.data.repository.AuthRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _registerState = MutableLiveData<Resource<Any>>()
    val registerState: LiveData<Resource<Any>> = _registerState

    fun register(request: RegisterRequest) {
        _registerState.value = Resource.Loading    // ✅ No brackets!
        viewModelScope.launch {
            _registerState.value = repository.register(request)
        }
    }
}