package com.iffelse.iastro.model.response

import com.google.gson.annotations.SerializedName

data class GetTimeResponseModel(

	@field:SerializedName("get_time")
	val getTime: GetTime? = null,

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class GetTime(

	@field:SerializedName("astrologer_phone")
	val astrologerPhone: String? = null,

	@field:SerializedName("total_cost")
	val totalCost: String? = null,

	@field:SerializedName("booked_end_time")
	val bookedEndTime: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("booked_start_time")
	val bookedStartTime: String? = null,

	@field:SerializedName("astrologer_name")
	val astrologerName: String? = null,

	@field:SerializedName("booking_id")
	val bookingId: String? = null,

	@field:SerializedName("time_remaining")
	val timeRemaining: String? = null,

	@field:SerializedName("slot_id")
	val slotId: String? = null,

	@field:SerializedName("user_phone")
	val userPhone: String? = null,

	@field:SerializedName("call_duration_minutes")
	val callDurationMinutes: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("timestamp")
	val timestamp: String? = null
)
