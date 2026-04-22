package com.io.lkconsultants.model

import com.google.gson.annotations.SerializedName

// LoginRequest.kt
data class LoginRequest(
    val email: String,
    val password: String
)

// LoginResponse.kt
data class LoginResponse(
    val message: String,
    val token: String,
    val user: User
)

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,        // "superadmin" | "admin" | "employee"
    val status: String,
    val joined_date: String,
    val is_online: Boolean,
    val last_seen: String,
    val created_at: String,
    val updated_at: String
)

data class UserStatus(
    val id: Int,
    val name: String,
    @SerializedName("is_online") val isOnline: Boolean,
    @SerializedName("last_seen") val lastSeen: String?
)