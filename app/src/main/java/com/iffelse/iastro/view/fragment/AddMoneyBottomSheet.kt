package com.iffelse.iastro.view.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iffelse.iastro.R

class AddMoneyBottomSheet(private val walletBalance: Double, private val onProceed: (Double) -> Unit) : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_add_money, null)
        dialog.setContentView(view)


        val etAmount = view.findViewById<EditText>(R.id.etAmount)
        val tvWalletBalance = view.findViewById<TextView>(R.id.tv_wallet_balance)
        val btn100 = view.findViewById<Button>(R.id.btn100)
        val btn500 = view.findViewById<Button>(R.id.btn500)
        val btn1000 = view.findViewById<Button>(R.id.btn1000)
        val btn50 = view.findViewById<Button>(R.id.btn50)
        val btn200 = view.findViewById<Button>(R.id.btn200)
        val btn150 = view.findViewById<Button>(R.id.btn150)
        val btnProceed = view.findViewById<Button>(R.id.btnProceed)

        tvWalletBalance.text =
            buildString {
                append("Current Wallet Balance: Rs. ")
                append(walletBalance)
            }

        // Set quick amount selection
        btn100.setOnClickListener { etAmount.setText("100") }
        btn500.setOnClickListener { etAmount.setText("500") }
        btn1000.setOnClickListener { etAmount.setText("1000") }
        btn50.setOnClickListener { etAmount.setText("50") }
        btn150.setOnClickListener { etAmount.setText("150") }
        btn200.setOnClickListener { etAmount.setText("200") }

        // Proceed button click
        btnProceed.setOnClickListener {
            val amount = etAmount.text.toString().trim()
            if (amount.isNotEmpty()) {
                onProceed(amount.toDouble()) // Send amount back to activity/fragment
                dismiss() // Close the bottom sheet
            } else {
                etAmount.error = "Enter a valid amount"
            }
        }

        return dialog
    }
}
