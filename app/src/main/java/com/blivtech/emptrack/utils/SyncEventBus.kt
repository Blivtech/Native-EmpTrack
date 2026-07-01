package com.blivtech.emptrack.utils

import androidx.lifecycle.MutableLiveData

object SyncEventBus {
    val syncCompleted = MutableLiveData<Long>()

    fun notifySyncComplete() {
        syncCompleted.value = System.currentTimeMillis()
    }
}