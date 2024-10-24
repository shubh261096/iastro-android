package com.iffelse.iastro.model.response

import com.google.gson.annotations.SerializedName

data class AstrologerResponseModel(

    @field:SerializedName("data")
    val data: List<DataItem?>? = null,

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null
)

data class DataItem(

    @field:SerializedName("specialty")
    val specialty: String? = null,

    @field:SerializedName("is_active")
    val isActive: String? = null,

    @field:SerializedName("preferred_languages")
    val preferredLanguages: String? = null,

    @field:SerializedName("final_rate")
    val finalRate: String? = null,

    @field:SerializedName("description_long")
    val descriptionLong: String? = null,

    @field:SerializedName("photo")
    val photo: String? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,

    @field:SerializedName("priority")
    val priority: String? = null,

    @field:SerializedName("experience")
    val experience: String? = null,

    @field:SerializedName("initial_rate")
    val initialRate: String? = null,

    @field:SerializedName("description_short")
    val descriptionShort: String? = null,

    @field:SerializedName("ratings")
    val ratings: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("phone_number")
    val phoneNumber: String? = null,

    @field:SerializedName("is_online")
    val isOnline: String? = null,

    @field:SerializedName("reviews_count")
    val reviewsCount: String? = null
)
