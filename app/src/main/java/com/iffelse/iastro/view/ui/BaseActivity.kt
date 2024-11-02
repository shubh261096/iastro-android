package com.iffelse.iastro.view.ui

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
