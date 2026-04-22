package com.io.lkconsultants.model

import com.google.gson.annotations.SerializedName

data class SendMessageResponse(
    @SerializedName("id")             val id: Long,
    @SerializedName("conversationId") val conversationId: Int,
    @SerializedName("sender")         val sender: SenderDto,
    @SerializedName("text")           val text: String,
    @SerializedName("fileUrl")        val fileUrl: String?,
    @SerializedName("createdAt")      val createdAt: String   // ISO-8601
)

data class SenderDto(
    @SerializedName("id")       val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("avatar")   val avatar: String?
)