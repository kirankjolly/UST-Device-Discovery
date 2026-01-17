package com.ust.discovery.ui.detail

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ust.discovery.R
import com.ust.discovery.databinding.ActivityDeviceDetailBinding
import com.ust.discovery.domain.Device

class DeviceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceDetailBinding
    private lateinit var device: Device

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDeviceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_DEVICE, Device::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_DEVICE)
        } ?: run {
            finish()
            return
        }

        setupToolbar()
        displayDeviceInfo()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun displayDeviceInfo() {
        binding.deviceNameText.text = device.name
        binding.deviceIpText.text = device.ipAddress
        binding.devicePortText.text = device.port.toString()
        binding.deviceServiceTypeText.text = device.serviceType
        binding.statusIndicator.isSelected = device.isOnline
        binding.deviceStatusText.text = if (device.isOnline) getString(R.string.online) else getString(R.string.offline)
    }

    companion object {
        const val EXTRA_DEVICE = "extra_device"
    }
}
