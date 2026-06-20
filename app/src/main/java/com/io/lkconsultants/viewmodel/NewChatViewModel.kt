package com.io.lkconsultants.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.io.lkconsultants.model.ChatUser
import com.io.lkconsultants.model.CreateConversationRequest
import com.io.lkconsultants.model.CreatedConversation
import com.room.roomy.retrofit.RetrofitInstance
import com.room.roomy.retrofit.TokenProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NewChatState {
    object Idle : NewChatState()
    object Loading : NewChatState()
    data class Success(val users: List<ChatUser>) : NewChatState()
    data class Error(val message: String) : NewChatState()
}

sealed class CreateConversationState {
    object Idle : CreateConversationState()
    object Loading : CreateConversationState()
    data class Success(val conversation: CreatedConversation, val target: ChatUser) : CreateConversationState()
    data class Error(val message: String) : CreateConversationState()
}

class NewChatViewModel : ViewModel() {

    private val _state = MutableStateFlow<NewChatState>(NewChatState.Idle)
    val state: StateFlow<NewChatState> = _state.asStateFlow()

    private val _createState = MutableStateFlow<CreateConversationState>(CreateConversationState.Idle)
    val createState: StateFlow<CreateConversationState> = _createState.asStateFlow()

    init { fetchUsers() }

    fun fetchUsers() {
        viewModelScope.launch {
            _state.value = NewChatState.Loading
            try {
                val response = RetrofitInstance.retrofits.getUsers()
                if (response.isSuccessful && response.body() != null) {
                    val me = TokenProvider.getUserId().toIntOrNull() ?: -1
                    val users = response.body()!!.users.filter { it.id != me }
                    _state.value = NewChatState.Success(users)
                } else {
                    _state.value = NewChatState.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = NewChatState.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    fun startConversationWith(user: ChatUser) {
        viewModelScope.launch {
            _createState.value = CreateConversationState.Loading
            try {
                val me = TokenProvider.getUserId().toIntOrNull() ?: -1
                val response = RetrofitInstance.retrofits.createConversation(
                    CreateConversationRequest(participantIds = listOf(me, user.id))
                )
                if (response.isSuccessful && response.body() != null) {
                    _createState.value = CreateConversationState.Success(response.body()!!, user)
                } else {
                    _createState.value = CreateConversationState.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _createState.value = CreateConversationState.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    fun resetCreateState() { _createState.value = CreateConversationState.Idle }
}
