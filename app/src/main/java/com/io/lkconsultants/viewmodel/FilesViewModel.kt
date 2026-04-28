package com.io.lkconsultants.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.io.lkconsultants.model.FilesResponse
import com.io.lkconsultants.model.Message
import com.io.lkconsultants.model.MessageResponse
import com.room.roomy.retrofit.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FilesState {
    object Idle    : FilesState()
    object Loading : FilesState()
    data class Success(val data: FilesResponse) : FilesState()
    data class Error(val message: String)       : FilesState()
}


sealed class MessagesState {
    object Idle : MessagesState()
    object Loading : MessagesState()
    data class Success(val messages: List<MessageResponse>) : MessagesState()
    data class Error(val message: String) : MessagesState()
}

//sealed class MessagesState {
//    object Idle    : MessagesState()
//    object Loading : MessagesState()
//    data class Success(val data: Message) : MessagesState()
//    data class SingleMessage(val message: MessageResponse) : MessagesState()
//    data class Error(val message: String) : MessagesState()
//}

class FilesViewModel : ViewModel() {

    private val _state = MutableStateFlow<FilesState>(FilesState.Idle)
    val state: StateFlow<FilesState> = _state.asStateFlow()

    fun getFiles(
        type: String? = null,
        userId: Int?  = null,
        page: Int     = 1,
        perPage: Int  = 20
    ) {
        viewModelScope.launch {
            _state.value = FilesState.Loading
            try {
                val response = RetrofitInstance.retrofits.getFiles("all", userId, page, perPage)
                if (response.isSuccessful) {
                    _state.value = FilesState.Success(response.body()!!)
                } else {
                    _state.value = FilesState.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = FilesState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    // Helper — format bytes → "240 KB", "1.2 MB" etc.
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
            bytes >= 1_024     -> "%.0f KB".format(bytes / 1_024.0)
            else               -> "$bytes B"
        }
    }
}