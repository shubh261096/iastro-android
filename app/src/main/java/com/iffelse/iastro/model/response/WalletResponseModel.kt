package com.iffelse.iastro.model.response

import com.google.gson.annotations.SerializedName

data class WalletResponseModel(

	@field:SerializedName("wallet_balance")
	val walletBalance: WalletBalance? = null,

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class WalletBalance(

	@field:SerializedName("balance")
	val balance: String? = null,

	@field:SerializedName("is_free")
	val isFree: String? = null
)