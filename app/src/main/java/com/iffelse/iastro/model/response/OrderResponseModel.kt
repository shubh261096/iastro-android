package com.iffelse.iastro.model.response

import com.google.gson.annotations.SerializedName

data class OrderResponseModel(

	@field:SerializedName("order_status")
	val orderStatus: String? = null,

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("order_id")
	val orderId: String? = null,

	@field:SerializedName("merchant_key")
	val merchantKey: String? = null
)
