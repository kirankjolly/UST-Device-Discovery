package com.ust.discovery.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
        setupSwipeRefresh()
        observeDevices()
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter { device ->
            navigateToDeviceDetail(device)
        }
        binding.devicesRecyclerView.adapter = deviceAdapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshDevices()
        }
    }

    private fun navigateToDeviceDetail(device: com.ust.discovery.domain.Device) {
        val intent = Intent(this, DeviceDetailActivity::class.java)
        intent.putExtra(DeviceDetailActivity.EXTRA_DEVICE, device)
        startActivity(intent)
    }

    private fun observeDevices() {
        lifecycleScope.launch {
            try {
                viewModel.devices.collect { devices ->
                    binding.swipeRefresh.isRefreshing = false
                    deviceAdapter.submitList(devices)

                    if (devices.isEmpty()) {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.devicesRecyclerView.visibility = View.GONE
                    } else {
                        binding.emptyStateLayout.visibility = View.GONE
                        binding.devicesRecyclerView.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing devices", e)
                binding.swipeRefresh.isRefreshing = false
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.devicesRecyclerView.visibility = View.GONE
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
