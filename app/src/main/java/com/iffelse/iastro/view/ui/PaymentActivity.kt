package com.iffelse.iastro.view.ui

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.iffelse.iastro.BuildConfig
import com.iffelse.iastro.R
import com.iffelse.iastro.model.BaseErrorModel
import com.iffelse.iastro.model.response.CommonResponseModel
import com.iffelse.iastro.model.response.OrderResponseModel
import com.iffelse.iastro.utils.AppConstants
import com.iffelse.iastro.utils.KeyStorePref
import com.iffelse.iastro.utils.OkHttpNetworkProvider
import com.iffelse.iastro.utils.Utils
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject


class PaymentActivity : AppCompatActivity(),
    PaymentResultWithDataListener {

    private var paymentOrderID = ""
    private var merchantKey = ""
    private var amount = 0.00
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        if (intent.extras != null) {
            if (intent.getDoubleExtra("amount", 0.00) > 0) {
                amount = intent.getDoubleExtra("amount", 0.00)
                createOrderRequest(amount)
            }
        }
    }

    private fun doRazorPay() {
        val co = Checkout()
        co.setKeyID(this.merchantKey)
        try {
            val options = JSONObject()
            options.put("name", "iAstro")
            options.put("description", "Add Money to wallet")
            options.put("image", "https://iastro.org/primary.png")
            options.put("theme.color", "#F48534")
            options.put("order_id", paymentOrderID)
            options.put("send_sms_hash", true)
            options.put("currency", "INR")
            options.put("amount", amount)
            val preFill = JSONObject()
            preFill.put("email", "shubham@lazyclick.in")
            preFill.put(
                "contact",
                KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)?.substring(2)
            )
            options.put("prefill", preFill)
            co.open(this, options)
        } catch (e: java.lang.Exception) {
            Toast.makeText(
                this@PaymentActivity,
                "Error in payment: " + e.message,
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }
    }

    private fun createOrderRequest(amount: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            val headers = mutableMapOf<String, String>()
            headers["Content-Type"] = "application/json"
            headers["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)

            val jsonObjectBody = JSONObject()
            jsonObjectBody.put("amount", amount)
            jsonObjectBody.put("currency", "INR")
            OkHttpNetworkProvider.post(
                BuildConfig.BASE_URL + "order/create_order_rzp",
                jsonObjectBody,
                headers,
                null,
                null,
                OrderResponseModel::class.java,
                object : OkHttpNetworkProvider.NetworkListener<OrderResponseModel> {
                    override fun onResponse(response: OrderResponseModel?) {
                        if (response != null) {
                            if (response.error == false) {
                                if (!response.orderId.isNullOrEmpty()) {
                                    paymentOrderID = response.orderId
                                }

                                if (!response.merchantKey.isNullOrEmpty()) {
                                    merchantKey = response.merchantKey
                                }
                                lifecycleScope.launch(Dispatchers.Main) {
                                    doRazorPay()
                                }
                            }
                        }
                        Log.i(TAG, "onResponse: $response")
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(
                                this@PaymentActivity,
                                error?.message ?: "Something went wrong!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            finish()
                        }
                    }

                })
        }
    }

    private fun verifyOrderRequest(paymentId: String, signature: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val headers = mutableMapOf<String, String>()
            headers["Content-Type"] = "application/json"
            headers["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)

            val jsonObjectBody = JSONObject()
            jsonObjectBody.put("order_id", this@PaymentActivity.paymentOrderID)
            jsonObjectBody.put("payment_id", paymentId)
            jsonObjectBody.put("signature", signature)

            OkHttpNetworkProvider.post(
                BuildConfig.BASE_URL + "order/verify_order_rzp",
                jsonObjectBody,
                headers,
                null,
                null,
                CommonResponseModel::class.java,
                object : OkHttpNetworkProvider.NetworkListener<CommonResponseModel> {
                    override fun onResponse(response: CommonResponseModel?) {
                        if (response != null) {
                            if (response.error == false) {
                                // Update wallet balance text view
                                lifecycleScope.launch(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@PaymentActivity,
                                        response.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                            } else {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@PaymentActivity,
                                        response.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                }
                            }
                        }
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(this@PaymentActivity, error?.message, Toast.LENGTH_SHORT)
                                .show()
                            finish()
                        }
                    }
                })
        }
    }


    companion object {
        private const val TAG = "PaymentActivity"
    }


    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
        Log.i(TAG, "onPaymentSuccess: $p0")
        p1?.paymentId?.let { p1.signature?.let { it1 -> verifyOrderRequest(it, it1) } }
    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        Log.i(TAG, "onPaymentError: $p1")
        Toast.makeText(
            this@PaymentActivity,
            "Something went wrong!",
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }
}