package com.iffelse.iastro.model.response

import com.google.gson.annotations.SerializedName

data class ChatTokenResponseModel(

	@field:SerializedName("chat_token")
	val chatToken: String? = null,

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)
