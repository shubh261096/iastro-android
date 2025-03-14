package com.iffelse.iastro.utils

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

object RemoteConfigUtils {

    private const val TAG = "RemoteConfigUtils"
    private const val DEFAULT_FETCH_INTERVAL = 3600L  // Fetch interval in seconds (1 hour)

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        Firebase.remoteConfig.apply {
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = DEFAULT_FETCH_INTERVAL
            }
            setConfigSettingsAsync(configSettings)
        }
    }

    /**
     * Fetch data from Firebase Remote Config using a dynamic key.
     * @param key The configuration key to fetch.
     * @param onComplete Callback with the fetched config value.
     */
    fun fetchData(key: String, defaultValue: String, onComplete: (String) -> Unit) {
        // Set a default value for the key if needed
        remoteConfig.setDefaultsAsync(mapOf(key to defaultValue))

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val dataString = remoteConfig.getString(key)
                    Log.d(TAG, "Fetched data for key '$key': $dataString")
                    onComplete(dataString)
                } else {
                    Log.e(TAG, "Fetch failed for key '$key'")
                    onComplete(remoteConfig.getString(key))  // Return default value if fetch failed
                }
            }
    }

    fun fetchData(key: String, defaultValue: Int, onComplete: (Long) -> Unit) {
        // Set a default value for the key if needed
        remoteConfig.setDefaultsAsync(mapOf(key to defaultValue))

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val dataString = remoteConfig.getLong(key)
                    Log.d(TAG, "Fetched data for key '$key': $dataString")
                    onComplete(dataString)
                } else {
                    Log.e(TAG, "Fetch failed for key '$key'")
                    onComplete(remoteConfig.getLong(key))  // Return default value if fetch failed
                }
            }
    }

    fun fetchData(key: String, defaultValue: Boolean, onComplete: (Boolean) -> Unit) {
        // Set a default value for the key if needed
        remoteConfig.setDefaultsAsync(mapOf(key to defaultValue))

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val dataString = remoteConfig.getBoolean(key)
                    Log.d(TAG, "Fetched data for key '$key': $dataString")
                    onComplete(dataString)
                } else {
                    Log.e(TAG, "Fetch failed for key '$key'")
                    onComplete(remoteConfig.getBoolean(key))  // Return default value if fetch failed
                }
            }
    }
}
