package com.ust.discovery.ui.detail

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ust.discovery.R
import com.ust.discovery.data.NetworkClient
import com.ust.discovery.databinding.ActivityDeviceDetailBinding
import com.ust.discovery.domain.Device
import kotlinx.coroutines.launch

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
        fetchPublicIp()
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

    private fun fetchPublicIp() {
        lifecycleScope.launch {
            val result = NetworkClient.get(IPIFY_API_URL)
            result.onSuccess { publicIp ->
                binding.publicIpText.text = publicIp
                fetchGeoInfo(publicIp)
            }.onFailure { error ->
                binding.publicIpText.text = getString(R.string.error_fetching_ip)
                displayGeoError()
            }
        }
    }

    private fun fetchGeoInfo(ip: String) {
        lifecycleScope.launch {
            val result = NetworkClient.getIpGeoInfo(ip)
            result.onSuccess { geoInfo ->
                binding.geoCity.text = geoInfo.city
                binding.geoRegion.text = geoInfo.region
                binding.geoCountry.text = geoInfo.country
                binding.geoLocation.text = geoInfo.loc
                binding.geoOrganization.text = geoInfo.org
                binding.geoTimezone.text = geoInfo.timezone
            }.onFailure { error ->
                displayGeoError()
            }
        }
    }

    private fun displayGeoError() {
        val errorText = getString(R.string.error_fetching_geo)
        binding.geoCity.text = errorText
        binding.geoRegion.text = errorText
        binding.geoCountry.text = errorText
        binding.geoLocation.text = errorText
        binding.geoOrganization.text = errorText
        binding.geoTimezone.text = errorText
    }

    companion object {
        const val EXTRA_DEVICE = "extra_device"
        private const val IPIFY_API_URL = "https://api.ipify.org"
    }
}
