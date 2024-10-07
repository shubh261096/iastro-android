package com.iffelse.iastro.model

data class Astrologer(
    val name: String,
    val specialty: String,
    val rating: Double,
    val reviews: Int,
    val description: String,
    val photo: String // Drawable resource ID for the astrologer photo
)
