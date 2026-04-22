package com.io.lkconsultants.model

// FileModels.kt

data class SharedByUser(
    val id: Int,
    val name: String,
    val role: String
)

data class SharedWithUser(
    val id: Int,
    val name: String,
    val role: String
)

data class ApiSharedFile(
    val id: Int,
    val file_name: String,
    val file_url: String,
    val file_size: Long,           // bytes — use Long
    val mime_type: String,
    val shared_by: SharedByUser,
    val shared_with: SharedWithUser,
    val message: String?,          // nullable — may not always have a message
    val created_at: String
)

data class Pagination(
    val current_page: Int,
    val per_page: Int,
    val total: Int,
    val last_page: Int
)

data class FilesResponse(
    val files: List<ApiSharedFile>,
    val pagination: Pagination
)