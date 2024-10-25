package com.iffelse.iastro.model.response.slots

import com.google.gson.annotations.SerializedName

data class AllBookingsResponseModel(

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("bookings")
	val bookings: List<BookingsItem?>? = null
)

data class BookingsItem(

	@field:SerializedName("booking_id")
	val bookingId: String? = null,

	@field:SerializedName("total_cost")
	val totalCost: String? = null,

	@field:SerializedName("slot_id")
	val slotId: String? = null,

	@field:SerializedName("user_phone")
	val userPhone: String? = null,

	@field:SerializedName("call_duration_minutes")
	val callDurationMinutes: String? = null,

	@field:SerializedName("booked_end_time")
	val bookedEndTime: String? = null,

	@field:SerializedName("booked_start_time")
	val bookedStartTime: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("timestamp")
	val timestamp: String? = null
)
