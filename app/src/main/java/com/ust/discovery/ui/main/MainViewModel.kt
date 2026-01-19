package com.ust.discovery.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ust.discovery.data.NsdDiscoveryManager
import com.ust.discovery.domain.Device
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val nsdDiscoveryManager = NsdDiscoveryManager(application)
    private var discoveryJob: Job? = null

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
        discoveryJob?.cancel()
        discoveryJob = viewModelScope.launch {
            try {
                nsdDiscoveryManager.initialize()
                nsdDiscoveryManager.discoverDevices().collect { }
            } catch (e: Exception) {
                Log.e(TAG, "Error during device discovery", e)
            }
        }
    }

    fun refreshDevices() {
        discoveryJob?.cancel()
        discoveryJob = viewModelScope.launch {
            try {
                Log.d(TAG, "Refreshing devices")
                nsdDiscoveryManager.initialize()
                nsdDiscoveryManager.discoverDevices().collect { }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing devices", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            nsdDiscoveryManager.cleanup()
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}
