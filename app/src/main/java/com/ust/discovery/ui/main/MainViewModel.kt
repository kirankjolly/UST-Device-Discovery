package com.ust.discovery.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ust.discovery.data.NsdDiscoveryManager
import com.ust.discovery.domain.Device
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val nsdDiscoveryManager = NsdDiscoveryManager(application)

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>> = _devices.asStateFlow()

    init {
        startDiscovery()
    }

    private fun startDiscovery() {
        viewModelScope.launch {
            nsdDiscoveryManager.discoverDevices().collect { }
        }

        viewModelScope.launch {
            nsdDiscoveryManager.discoveredDevices.collect { deviceList ->
                _devices.value = deviceList
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        nsdDiscoveryManager.cleanup()
    }
}
