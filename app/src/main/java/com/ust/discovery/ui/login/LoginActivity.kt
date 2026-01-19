package com.ust.discovery.ui.login

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.ust.discovery.R
import com.ust.discovery.data.SessionManager
import com.ust.discovery.databinding.ActivityLoginBinding
import com.ust.discovery.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sessionManager: SessionManager

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleSignInResult(task)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        configureGoogleSignIn()

        checkExistingLogin()
        setupUI()
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupUI() {
        binding.btnGoogleSignIn.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun checkExistingLogin() {
        if (sessionManager.isLoggedIn()) {
            if (!isNetworkAvailable()) {
                forceLogout()
                return
            }

            attemptSilentSignIn()
        }
    }

    private fun attemptSilentSignIn() {
        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (lastSignedInAccount != null) {
            Log.d(TAG, "Silent sign-in successful")
            navigateToHome()
        } else {
            Log.d(TAG, "Silent sign-in failed, clearing session")
            sessionManager.clearSession()
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken != null) {
                Log.d(TAG, "ID Token: $idToken")
                sessionManager.saveToken(idToken)
                Toast.makeText(this, getString(R.string.auth_sign_in_success_message), Toast.LENGTH_SHORT).show()
                navigateToHome()
            } else {
                Log.e(TAG, "ID Token is null")
                showError(getString(R.string.auth_failed))
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Sign in failed: ${e.statusCode}", e)
            when (e.statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> showError(getString(R.string.auth_cancelled))
                else -> showError(getString(R.string.auth_failed))
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun forceLogout() {
        Log.d(TAG, "Network unavailable, forcing logout")
        sessionManager.clearSession()
        googleSignInClient.signOut()
        Toast.makeText(this, getString(R.string.auth_network_unavailable), Toast.LENGTH_LONG).show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
