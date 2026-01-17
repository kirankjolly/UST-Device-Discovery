package com.ust.discovery.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ust.discovery.data.NsdDiscoveryManager
import com.ust.discovery.domain.Device
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val nsdDiscoveryManager = NsdDiscoveryManager(application)

    val devices: StateFlow<List<Device>> = nsdDiscoveryManager.allDevices
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        startDiscovery()
    }

    private fun startDiscovery() {
        viewModelScope.launch {
            nsdDiscoveryManager.initialize()
            nsdDiscoveryManager.discoverDevices().collect { }
        }
    }

    override fun onCleared() {
        super.onCleared()
        nsdDiscoveryManager.cleanup()
    }
}
