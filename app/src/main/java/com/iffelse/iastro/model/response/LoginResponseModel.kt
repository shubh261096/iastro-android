package com.iffelse.iastro.model.response

import com.google.gson.annotations.SerializedName

data class LoginResponseModel(

    @field:SerializedName("user_status")
    val userStatus: String? = null,

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("user")
    val user: User? = null
)

data class User(

    @field:SerializedName("place_of_birth")
    val placeOfBirth: String? = null,

    @field:SerializedName("time_of_birth")
    val timeOfBirth: String? = null,

    @field:SerializedName("preferred_languages")
    val preferredLanguages: String? = null,

    @field:SerializedName("is_active")
    val isActive: String? = null,

    @field:SerializedName("gender")
    val gender: String? = null,

    @field:SerializedName("dob")
    val dob: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,

    @field:SerializedName("phone_number")
    val phoneNumber: String? = null
)
