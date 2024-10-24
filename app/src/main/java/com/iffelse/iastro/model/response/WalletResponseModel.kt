package com.iffelse.iastro.model.response

import com.google.gson.annotations.SerializedName

data class WalletResponseModel(

    @field:SerializedName("wallet_balance")
    val walletBalance: String? = null,

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null
)
