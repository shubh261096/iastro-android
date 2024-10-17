package com.iffelse.iastro.model

data class Astrologer(
    val profileData: ProfileData? = null,
    val availability: Availability? = null
)

data class ProfileData(
    val name: String? = null,
    val specialty: String? = null,
    val rating: Float? = null,
    val reviews: Int? = null,
    val description: String? = null,
    val photo: String? = null,
    val rate: String? = null,
    val isActive: Boolean? = null,
    val isOnline: Boolean? = null
)

data class Availability(
    val timeSlots: List<TimeSlot>? = null
)
data class TimeSlot(
    val startTime: String? = null,
    val endTime: String? = null,
    val interval: Int? = null
)

