package com.iffelse.iastro.model.response.slots

import com.iffelse.iastro.utils.Utils

data class TimeSlot(
    val startTime: String,
    val endTime: String,
    val isBooked: Boolean
) {
    val displayTime: String
        get() = "${Utils.convertTo12HourFormatWithoutSeconds(startTime)}"
}
