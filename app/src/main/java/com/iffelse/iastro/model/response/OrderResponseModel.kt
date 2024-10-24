package com.iffelse.iastro.model.response

import com.google.gson.annotations.SerializedName

data class OrderResponseModel(

	@field:SerializedName("order_status")
	val orderStatus: String? = null,

	@field:SerializedName("cf_order_id")
	val cfOrderId: String? = null,

	@field:SerializedName("payment_session_id")
	val paymentSessionId: String? = null,

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("order_id")
	val orderId: String? = null
)
