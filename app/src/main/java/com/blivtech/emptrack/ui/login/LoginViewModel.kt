package com.blivtech.emptrack.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.model.LoginRequest
import com.blivtech.emptrack.data.model.LoginResponse
import com.blivtech.emptrack.data.repository.AuthRepository
import com.blivtech.emptrack.utils.PreferenceManager
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val preferenceManager: PreferenceManager  // ✅ Inject
) : ViewModel() {

    private val _loginState = MutableLiveData<Resource<LoginResponse>>()
    val loginState: LiveData<Resource<LoginResponse>> = _loginState

    fun login(request: LoginRequest) {
        _loginState.value = Resource.Loading
        viewModelScope.launch {
            _loginState.value = repository.login(request)
        }
    }

    // ✅ Save login data to DataStore
    suspend fun saveLoginData(
        token: String,
        btCode: String,
        userName: String,
        phone: String,
        userType: Int
    ) {
        preferenceManager.saveLoginData(
            token    = token,
            btCode   = btCode,
            userName = userName,
            phone    = phone,
            userType = userType
        )
    }
}