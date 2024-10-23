package com.iffelse.iastro

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cashfree.pg.api.CFPaymentGatewayService
import com.cashfree.pg.base.exception.CFException
import com.cashfree.pg.core.api.CFSession
import com.cashfree.pg.core.api.callback.CFCheckoutResponseCallback
import com.cashfree.pg.core.api.utils.CFErrorResponse
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutPayment
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutTheme


class PaymentActivity : AppCompatActivity(), CFCheckoutResponseCallback {

    var orderID = "new_test_112123w12w213"
    var paymentSessionID = "session_70o9ixKF7_X74fzZNy6CwTb_bGxZytS-aVQziH-5DemWVWR21Hk6IKofTLQ_fPJKjUtQ_Ta6P6n2v6_3FYSsN3mDC7sHGqWx5sT0h8uG3HcQ"
    var cfEnvironment = CFSession.Environment.SANDBOX
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        try {
            CFPaymentGatewayService.getInstance().setCheckoutCallback(this)
        } catch (e: CFException) {
            e.printStackTrace()
        }
        doWebCheckoutPayment()
    }

    fun doWebCheckoutPayment() {
        try {
            val cfSession = CFSession.CFSessionBuilder()
                .setEnvironment(cfEnvironment)
                .setPaymentSessionID(paymentSessionID)
                .setOrderId(orderID)
                .build()
            val cfTheme = CFWebCheckoutTheme.CFWebCheckoutThemeBuilder()
                .setNavigationBarBackgroundColor("#000000")
                .setNavigationBarTextColor("#FFFFFF")
                .build()
            val cfWebCheckoutPayment = CFWebCheckoutPayment.CFWebCheckoutPaymentBuilder()
                .setSession(cfSession)
                .setCFWebCheckoutUITheme(cfTheme)
                .build()
            CFPaymentGatewayService.getInstance().doPayment(this@PaymentActivity, cfWebCheckoutPayment)
        } catch (exception: CFException) {
            exception.printStackTrace()
        }
    }

    override fun onPaymentVerify(orderID: String) {
        Log.d("onPaymentVerify", "verifyPayment triggered")
    }

    override fun onPaymentFailure(cfErrorResponse: CFErrorResponse, orderID: String) {
        Log.e("onPaymentFailure $orderID", cfErrorResponse.message)
    }
}