package com.iffelse.iastro.model.response

import com.google.gson.annotations.SerializedName

data class CommonResponseModel(

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null,
)

