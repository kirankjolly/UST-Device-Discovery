package com.ust.discovery.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.ust.discovery.R
import com.ust.discovery.data.SessionManager
import com.ust.discovery.databinding.ActivityMainBinding
import com.ust.discovery.ui.detail.DeviceDetailActivity
import com.ust.discovery.ui.login.LoginActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)
        setupRecyclerView()
        setupSwipeRefresh()
        observeDevices()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirmation)
            .setPositiveButton(R.string.logout) { _, _ ->
                performLogout()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun performLogout() {
        sessionManager.clearSession()
        googleSignInClient.signOut().addOnCompleteListener {
            Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
