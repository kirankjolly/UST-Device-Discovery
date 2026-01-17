package com.ust.discovery

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ust.discovery.data.NsdDiscoveryManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var nsdDiscoveryManager: NsdDiscoveryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNsdDiscovery()
    }

    private fun setupNsdDiscovery() {
        nsdDiscoveryManager = NsdDiscoveryManager(this)

        lifecycleScope.launch {
            nsdDiscoveryManager.discoverDevices().collect { device ->
                Log.d(TAG, "Device discovered: ${device.name} at ${device.ipAddress}")
            }
        }

        lifecycleScope.launch {
            nsdDiscoveryManager.discoveredDevices.collect { devices ->
                Log.d(TAG, "Total devices: ${devices.size}")
                devices.forEach { device ->
                    Log.d(TAG, "  - ${device.name} (${device.ipAddress}:${device.port})")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        nsdDiscoveryManager.cleanup()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
