package com.io.lkconsultants.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.io.lkconsultants.model.SendMessageResponse
import com.io.lkconsultants.reverb.ReverbManager
import com.room.roomy.retrofit.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


sealed class SendMessageState {
    object Idle : SendMessageState()
    object Loading : SendMessageState()
    data class Success(val message: SendMessageResponse) : SendMessageState()
    data class Error(val message: String) : SendMessageState()
}

class SendMessageViewModel : ViewModel() {

    private val _state = MutableStateFlow<SendMessageState>(SendMessageState.Idle)
    val state: StateFlow<SendMessageState> = _state.asStateFlow()

    fun sendMessage(
        conversationId: Int,
        text: String,
        file: File? = null
    ) {
        viewModelScope.launch {
            _state.value = SendMessageState.Loading

            try {
                val conversationIdPart = conversationId
                    .toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                val textPart = text
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                val filePart = file?.let {
                    val mimeType = resolveMimeType(it)
                    val requestBody = it.asRequestBody(mimeType.toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("file", it.name, requestBody)
                }

                val response = RetrofitInstance.retrofits.sendMessage(
                    conversationId = conversationIdPart,
                    text           = textPart,
                    file           = filePart
                )


                Log.d("SendMessage", response.body().toString())

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        _state.value = SendMessageState.Success(body)
                    } else {
                        _state.value = SendMessageState.Error("Empty response")
                    }
                } else {
                    Log.d("fddd",response.message())
                    _state.value = SendMessageState.Error(
                        "Failed: ${response.code()} - ${response.message()}"
                    )
                }

            } catch (e: Exception) {
                _state.value = SendMessageState.Error(
                    e.localizedMessage ?: "Network error"
                )
            }
        }
    }

    fun resetState() {
        _state.value = SendMessageState.Idle
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun resolveMimeType(file: File): String =
        when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png"         -> "image/png"
            "gif"         -> "image/gif"
            "webp"        -> "image/webp"
            "svg"         -> "image/svg+xml"
            "bmp"         -> "image/bmp"
            "pdf"         -> "application/pdf"
            "doc"         -> "application/msword"
            "docx"        -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls"         -> "application/vnd.ms-excel"
            "xlsx"        -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "txt"         -> "text/plain"
            "zip"         -> "application/zip"
            "rar"         -> "application/x-rar-compressed"
            else          -> "application/octet-stream"
        }
}