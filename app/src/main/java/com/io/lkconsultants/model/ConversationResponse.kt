package com.io.lkconsultants.model

data class ConversationResponse(
    val id: Int,
    val is_group: Boolean,
    val group_id: Int?,
    val group_name: String?,
    val last_message: String,
    val updated_at: String,
    val participants: List<Participant>
)

data class Participant(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val status: String
)