package com.iffelse.iastro.view.ui

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cashfree.pg.api.CFPaymentGatewayService
import com.cashfree.pg.base.exception.CFException
import com.cashfree.pg.core.api.CFSession
import com.cashfree.pg.core.api.callback.CFCheckoutResponseCallback
import com.cashfree.pg.core.api.utils.CFErrorResponse
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutPayment
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutTheme
import com.iffelse.iastro.BuildConfig
import com.iffelse.iastro.R
import com.iffelse.iastro.model.BaseErrorModel
import com.iffelse.iastro.model.response.LoginResponseModel
import com.iffelse.iastro.model.response.OrderResponseModel
import com.iffelse.iastro.utils.AppConstants
import com.iffelse.iastro.utils.KeyStorePref
import com.iffelse.iastro.utils.OkHttpNetworkProvider
import com.iffelse.iastro.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject


class PaymentActivity : AppCompatActivity(), CFCheckoutResponseCallback {

    private var paymentOrderID = ""
    private var paymentSessionID = ""
    private var cfEnvironment = CFSession.Environment.PRODUCTION
    private var amount = 0.00
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        try {
            CFPaymentGatewayService.getInstance().setCheckoutCallback(this)
        } catch (e: CFException) {
            e.printStackTrace()
        }

        if (intent.extras != null) {
            if (intent.getDoubleExtra("amount", 0.00) > 0) {
                amount = intent.getDoubleExtra("amount", 0.00)
            }
        }

        createOrderRequest(amount)
    }

    private fun createOrderRequest(amount: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            val headers = mutableMapOf<String, String>()
            headers["Content-Type"] = "application/json"
            headers["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)

            val jsonObjectBody = JSONObject()
            jsonObjectBody.put("order_amount", amount)
            jsonObjectBody.put("order_currency", "INR")
            val jsonObjectCustomerDetails = JSONObject()
            jsonObjectCustomerDetails.put(
                "customer_id",
                KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)
            )
            jsonObjectCustomerDetails.put(
                "customer_name",
                KeyStorePref.getString(AppConstants.KEY_STORE_NAME)
            )
            jsonObjectCustomerDetails.put("customer_email", "")
            jsonObjectCustomerDetails.put(
                "customer_phone",
                KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)
            )
            jsonObjectBody.put("customer_details", jsonObjectCustomerDetails)
            val orderId = Utils.generateUniquePaymentId()
            jsonObjectBody.put("order_id", orderId)
            val jsonObjectOrderMeta = JSONObject()
            jsonObjectOrderMeta.put(
                "return_url",
                "https://b8af79f41056.eu.ngrok.io?order_id=$orderId"
            )
            jsonObjectBody.put("order_meta", jsonObjectOrderMeta)

            OkHttpNetworkProvider.post(
                BuildConfig.BASE_URL + "order/create_order",
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

                                if (!response.paymentSessionId.isNullOrEmpty()) {
                                    paymentSessionID = response.paymentSessionId
                                }
                                doWebCheckoutPayment()
                            }
                        }
                        Log.i(TAG, "onResponse: $response")
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                        // TODO: Handle Error
                        Toast.makeText(this@PaymentActivity, error?.message ?: "Something went wrong!", Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    }

                })
        }
    }

    private fun verifyOrderRequest(orderID: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val headers = mutableMapOf<String, String>()
            headers["Content-Type"] = "application/json"
            headers["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)
            OkHttpNetworkProvider.get(
                BuildConfig.BASE_URL + "order/verify_order/$orderID",
                headers,
                null,
                null,
                LoginResponseModel::class.java,
                object : OkHttpNetworkProvider.NetworkListener<LoginResponseModel> {
                    override fun onResponse(response: LoginResponseModel?) {
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
                                Toast.makeText(
                                    this@PaymentActivity,
                                    response.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        }
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                        Toast.makeText(this@PaymentActivity, error?.message, Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    }
                })
        }
    }


    fun doWebCheckoutPayment() {
        try {
            val cfSession = CFSession.CFSessionBuilder()
                .setEnvironment(cfEnvironment)
                .setPaymentSessionID(paymentSessionID)
                .setOrderId(paymentOrderID)
                .build()
            val cfTheme = CFWebCheckoutTheme.CFWebCheckoutThemeBuilder()
                .setNavigationBarBackgroundColor("#000000")
                .setNavigationBarTextColor("#FFFFFF")
                .build()
            val cfWebCheckoutPayment = CFWebCheckoutPayment.CFWebCheckoutPaymentBuilder()
                .setSession(cfSession)
                .setCFWebCheckoutUITheme(cfTheme)
                .build()
            CFPaymentGatewayService.getInstance()
                .doPayment(this@PaymentActivity, cfWebCheckoutPayment)
        } catch (exception: CFException) {
            exception.printStackTrace()
        }
    }

    override fun onPaymentVerify(orderID: String) {
        Log.d(TAG, "verifyPayment triggered")
        verifyOrderRequest(orderID)
    }

    override fun onPaymentFailure(cfErrorResponse: CFErrorResponse, orderID: String) {
        Log.e(TAG, orderID + cfErrorResponse.message)
        Toast.makeText(
            this@PaymentActivity,
            cfErrorResponse.message ?: "Something went wrong!",
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }

    companion object {
        private const val TAG = "PaymentActivity"
    }
}