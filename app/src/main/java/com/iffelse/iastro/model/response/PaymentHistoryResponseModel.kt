package com.iffelse.iastro.model.response

import com.google.gson.annotations.SerializedName

data class PaymentHistoryResponseModel(

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("payment_history")
	val paymentHistory: List<PaymentHistoryItem?>? = null
)

data class PaymentHistoryItem(

	@field:SerializedName("transaction_date")
	val transactionDate: String? = null,

	@field:SerializedName("amount")
	val amount: String? = null,

	@field:SerializedName("payment_gateway_transaction_id")
	val paymentGatewayTransactionId: String? = null,

	@field:SerializedName("transaction_type")
	val transactionType: String? = null
)
