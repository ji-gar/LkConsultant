package com.io.lkconsultants.model

data class UsersListResponse(
    val users: List<ChatUser>
)

data class ChatUser(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val status: String? = null
)

data class CreateConversationRequest(
    val participantIds: List<Int>
)

data class CreatedConversation(
    val id: Int,
    val is_group: Boolean = false,
    val participants: List<Participant> = emptyList()
)
