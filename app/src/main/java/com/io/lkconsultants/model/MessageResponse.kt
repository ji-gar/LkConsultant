package com.io.lkconsultants.model

data class Message(
    var messages : List<MessageResponse>
)

data class MessageResponse(
    val id: Int,
    val conversation_id: Int,
    val sender_id: Int,
    val text: String?,
    val file_name: String?,
    val file_url: String?,
    val created_at: String,
    val sender: Sender
)

data class Sender(
    val id: Int,
    val name: String,
    val role: String
)