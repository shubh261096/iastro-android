package com.iffelse.iastro.model.response

import com.google.gson.annotations.SerializedName

data class AstrologerStatusResponseModel(

	@field:SerializedName("is_busy")
	val isBusy: Int? = null,

	@field:SerializedName("is_online")
	val isOnline: Int? = null,

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)
