package com.iffelse.iastro.view.ui


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.iffelse.iastro.BuildConfig
import com.iffelse.iastro.databinding.ActivityWalletBinding
import com.iffelse.iastro.model.BaseErrorModel
import com.iffelse.iastro.model.response.PaymentHistoryResponseModel
import com.iffelse.iastro.model.response.WalletResponseModel
import com.iffelse.iastro.utils.AppConstants
import com.iffelse.iastro.utils.KeyStorePref
import com.iffelse.iastro.utils.OkHttpNetworkProvider
import com.iffelse.iastro.utils.Utils
import com.iffelse.iastro.view.adapter.PaymentHistoryAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WalletActivity : BaseActivity() {

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityWalletBinding
    private lateinit var paymentHistoryAdapter: PaymentHistoryAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.toolbarImage.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Update wallet balance UI
        updateWalletBalanceUI()

        binding.rvTransactions.layoutManager =
            LinearLayoutManager(this@WalletActivity)

        fetchWalletTransaction()

        // Handle Add Money button click
        binding.btnAddMoney.setOnClickListener {
            val enteredAmount = binding.etAmount.text.toString()
            if (enteredAmount.isNotEmpty()) {
                val amount = enteredAmount.toDoubleOrNull()
                if (amount != null && amount > 0) {
                    addMoneyToWallet(amount)
                } else {
                    showToast("Please enter a valid amount")
                }
            } else {
                showToast("Amount cannot be empty")
            }
        }

        // Register the activity result launcher
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the result from SecondActivity
                updateWalletBalanceUI()
            }
        }
    }

    private fun updateWalletBalanceUI() {
        Utils.showProgress(this@WalletActivity, "Please wait...")
        lifecycleScope.launch(Dispatchers.IO) {
            val headers = mutableMapOf<String, String>()
            headers["Content-Type"] = "application/json"
            headers["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)
            OkHttpNetworkProvider.get(
                BuildConfig.BASE_URL + "wallet/get_balance/" + KeyStorePref.getString(
                    AppConstants.KEY_STORE_USER_ID
                ),
                headers,
                null,
                null,
                WalletResponseModel::class.java,
                object : OkHttpNetworkProvider.NetworkListener<WalletResponseModel> {
                    override fun onResponse(response: WalletResponseModel?) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Utils.hideProgress()
                        }
                        if (response != null) {
                            if (!response.walletBalance?.balance.isNullOrEmpty()) {
                                // Update wallet balance text view
                                lifecycleScope.launch(Dispatchers.Main) {
                                    binding.tvWalletBalance.text =
                                        buildString {
                                            append("Current Wallet Balance: Rs. ")
                                            append(response.walletBalance?.balance)
                                        }
                                }
                            }
                        }
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                        lifecycleScope.launch(Dispatchers.Main) {
                            Utils.hideProgress()
                            Toast.makeText(
                                this@WalletActivity,
                                error?.message,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                })
        }
    }

    private fun fetchWalletTransaction() {
        lifecycleScope.launch(Dispatchers.IO) {
            val headers = mutableMapOf<String, String>()
            headers["Content-Type"] = "application/json"
            headers["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)
            OkHttpNetworkProvider.get(
                BuildConfig.BASE_URL + "wallet/payment_history/" + KeyStorePref.getString(
                    AppConstants.KEY_STORE_USER_ID
                ),
                headers,
                null,
                null,
                PaymentHistoryResponseModel::class.java,
                object : OkHttpNetworkProvider.NetworkListener<PaymentHistoryResponseModel> {
                    override fun onResponse(response: PaymentHistoryResponseModel?) {
                        if (response != null) {
                            lifecycleScope.launch(Dispatchers.Main) {

                                if (!response.paymentHistory.isNullOrEmpty()) {
                                    paymentHistoryAdapter =
                                        PaymentHistoryAdapter(response.paymentHistory)
                                    binding.rvTransactions.adapter = paymentHistoryAdapter
                                    binding.clPaymentHistory.visibility = View.VISIBLE
                                } else {
                                    binding.clPaymentHistory.visibility = View.GONE
                                }
                            }
                        }
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(
                                this@WalletActivity,
                                error?.message,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                })
        }
    }

    private fun addMoneyToWallet(amount: Double) {
        val intent = Intent(this@WalletActivity, PaymentActivity::class.java)
        intent.putExtra("amount", amount * 100)
        activityResultLauncher.launch(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "WalletActivity"
    }
}
