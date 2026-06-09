package com.blivtech.emptrack.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blivtech.emptrack.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _isOnline = MutableLiveData<Boolean>()
    val isOnline: LiveData<Boolean> = _isOnline

    fun checkNetwork() {
        _isOnline.value = networkUtils.isInternetAvailable()
    }
    fun checkNetworkSync(): Boolean = networkUtils.isInternetAvailable()

}