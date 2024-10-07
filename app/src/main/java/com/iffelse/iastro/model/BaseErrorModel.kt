package com.iffelse.iastro.model
import com.google.gson.annotations.SerializedName

data class BaseErrorModel(
    @SerializedName("errorBody") val errorBody: ErrorBody?,
    @SerializedName("errorCode") val errorCode: Int?,
    @SerializedName("message") var message: String?,
    @SerializedName("errorDetail") val errorDetail: String?
)

data class ErrorBody(
    @SerializedName("status") val status: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("requestId") val requestId: String?,
    @SerializedName("message") var message: String?
)
