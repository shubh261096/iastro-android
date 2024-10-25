package com.iffelse.iastro.model.response.slots

import com.google.gson.annotations.SerializedName

data class AllSlotsResponseModel(

    @field:SerializedName("all_slots")
    val allSlots: List<AllSlotsItem?>? = null,

    @field:SerializedName("error")
    val error: Boolean? = null
)

data class AllSlotsItem(

    @field:SerializedName("start_time")
    val startTime: String? = null,

    @field:SerializedName("astrologer_phone")
    val astrologerPhone: String? = null,

    @field:SerializedName("slot_id")
    val slotId: String? = null,

    @field:SerializedName("end_time")
    val endTime: String? = null
)
