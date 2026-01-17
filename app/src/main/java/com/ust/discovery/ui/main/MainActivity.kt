package com.ust.discovery.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ust.discovery.databinding.ActivityMainBinding
import com.ust.discovery.ui.detail.DeviceDetailActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var deviceAdapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        observeDevices()
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter { device ->
            navigateToDeviceDetail(device)
        }
        binding.devicesRecyclerView.adapter = deviceAdapter
    }

    private fun navigateToDeviceDetail(device: com.ust.discovery.domain.Device) {
        val intent = Intent(this, DeviceDetailActivity::class.java)
        intent.putExtra(DeviceDetailActivity.EXTRA_DEVICE, device)
        startActivity(intent)
    }

    private fun observeDevices() {
        lifecycleScope.launch {
            viewModel.devices.collect { devices ->
                deviceAdapter.submitList(devices)

                if (devices.isEmpty()) {
                    binding.emptyStateText.visibility = View.VISIBLE
                    binding.devicesRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyStateText.visibility = View.GONE
                    binding.devicesRecyclerView.visibility = View.VISIBLE
                }
            }
        }
    }
}
