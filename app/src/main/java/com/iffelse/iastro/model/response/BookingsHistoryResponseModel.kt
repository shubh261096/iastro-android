package com.iffelse.iastro.model.response

import com.google.gson.annotations.SerializedName

data class BookingsHistoryResponseModel(

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("bookings_history")
	val bookingsHistory: List<BookingsHistoryItem?>? = null
)

data class BookingsHistoryItem(

	@field:SerializedName("booking_id")
	val bookingId: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("astrologer_phone")
	val astrologerPhone: String? = null,

	@field:SerializedName("total_cost")
	val totalCost: String? = null,

	@field:SerializedName("slot_id")
	val slotId: String? = null,

	@field:SerializedName("call_duration_minutes")
	val callDurationMinutes: String? = null,

	@field:SerializedName("booked_end_time")
	val bookedEndTime: String? = null,

	@field:SerializedName("booked_start_time")
	val bookedStartTime: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("timestamp")
	val timestamp: String? = null,

	@field:SerializedName("astrologer_name")
	val astrologerName: String? = null,

	@field:SerializedName("astrologer_photo")
	val astrologerPhoto: String? = null
)
