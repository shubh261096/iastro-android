package com.iffelse.iastro.view.ui

import android.Manifest
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.iffelse.iastro.view.receiver.NetworkChangeReceiver

open class BaseActivity : AppCompatActivity() {
    private var networkChangeReceiver: NetworkChangeReceiver? = null
    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the NetworkChangeReceiver
        networkChangeReceiver = NetworkChangeReceiver()
        registerReceiver(
            networkChangeReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )

        // Observe network changes
        observeInternetChange()

        // Only request on Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            askNotificationPermissionIfNecessary()
        }
    }

    // Public method to initiate the permission request
    // Method to check and request notification permission if needed
    // Check and request notification permission if needed
    private fun askNotificationPermissionIfNecessary() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }


    private fun observeInternetChange() {
        NetworkChangeReceiver.getStatus().observe(
            this
        ) { status: Boolean? ->
            if (status != null) {
                if (!status) {
                    showNoInternetSnackbar()
                } else {
                    hideNoInternetSnackbar()
                }
            }
        }
    }

    private fun showNoInternetSnackbar() {
        snackbar = Snackbar.make(
            findViewById(android.R.id.content),
            "No internet connection",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("Dismiss") { dismiss() }
            show()
        }
    }

    private fun hideNoInternetSnackbar() {
        snackbar?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        networkChangeReceiver?.let { unregisterReceiver(it) }
    }
}
