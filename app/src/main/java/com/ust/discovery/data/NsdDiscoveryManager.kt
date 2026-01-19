package com.ust.discovery.data

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.ust.discovery.domain.Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class NsdDiscoveryManager(context: Context) {

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val deviceDao = AppDatabase.getDatabase(context).deviceDao()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val allDevices: Flow<List<Device>> = deviceDao.getAllDevices()

    suspend fun initialize() {
        deviceDao.markAllDevicesOffline()
        Log.d(TAG, "All devices marked as offline on initialization")
    }

    fun discoverDevices(): Flow<Device> = callbackFlow {
        val serviceTypes = listOf(
            // Core service types
            "_http._tcp",
            "_airplay._tcp",
            "_ipp._tcp",
            "_printer._tcp",
            "_googlecast._tcp",

            // Additional service types for temporary testing
            "_workstation._tcp",
            "_ssh._tcp",
            "_smb._tcp",
            "_scanner._tcp",
            "_spotify-connect._tcp"
        )

        val discoveryListeners = mutableListOf<Pair<String, NsdManager.DiscoveryListener>>()

        serviceTypes.forEach { serviceType ->
            val discoveryListener = object : NsdManager.DiscoveryListener {
                override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                    Log.e(TAG, "Discovery failed for $serviceType: Error code $errorCode")
                }

                override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                    Log.e(TAG, "Stop discovery failed for $serviceType: Error code $errorCode")
                }

                override fun onDiscoveryStarted(regType: String?) {
                    Log.d(TAG, "Discovery started for $regType")
                }

                override fun onDiscoveryStopped(serviceType: String?) {
                    Log.d(TAG, "Discovery stopped for $serviceType")
                }

                override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
                    serviceInfo?.let {
                        Log.d(TAG, "Service found: ${it.serviceName}")
                        resolveService(it)
                    }
                }

                override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
                    serviceInfo?.let {
                        Log.d(TAG, "Service lost: ${it.serviceName}")
                    }
                }
            }

            try {
                nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
                discoveryListeners.add(serviceType to discoveryListener)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting discovery for $serviceType", e)
            }
        }

        awaitClose {
            discoveryListeners.forEach { (serviceType, listener) ->
                try {
                    nsdManager.stopServiceDiscovery(listener)
                    Log.d(TAG, "Stopped discovery for $serviceType")
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping discovery for $serviceType", e)
                }
            }
        }
    }

    private fun resolveService(serviceInfo: NsdServiceInfo) {
        val resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                Log.e(TAG, "Resolve failed for ${serviceInfo?.serviceName}: Error code $errorCode")
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
                serviceInfo?.let {
                    val ipAddress = it.host?.hostAddress ?: ""
                    val deviceName = it.serviceName
                    val port = it.port
                    val serviceType = it.serviceType

                    if (ipAddress.isNotEmpty()) {
                        scope.launch {
                            try {
                                val existingDevice = deviceDao.getDevice(deviceName, ipAddress)
                                if (existingDevice != null) {
                                    deviceDao.updateDeviceStatus(deviceName, ipAddress, true)
                                    Log.d(TAG, "Device status updated to online: $deviceName at $ipAddress:$port")
                                } else {
                                    val device = Device(
                                        name = deviceName,
                                        ipAddress = ipAddress,
                                        port = port,
                                        serviceType = serviceType,
                                        isOnline = true
                                    )
                                    deviceDao.insertDevice(device)
                                    Log.d(TAG, "New device discovered and saved: $deviceName at $ipAddress:$port")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error updating device status: $deviceName", e)
                            }
                        }
                    }
                }
            }
        }

        try {
            nsdManager.resolveService(serviceInfo, resolveListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving service: ${serviceInfo.serviceName}", e)
        }
    }

    fun cleanup() {
        try {
            scope.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling scope", e)
        }
    }

    companion object {
        private const val TAG = "NsdDiscoveryManager"
    }
}
