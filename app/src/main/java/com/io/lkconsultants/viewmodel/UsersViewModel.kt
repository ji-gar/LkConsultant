package com.io.lkconsultants.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.io.lkconsultants.model.ConversationResponse
import com.io.lkconsultants.model.MessageResponse
import com.io.lkconsultants.reverb.ChatChannelListener
import com.io.lkconsultants.reverb.ChatNotifier
import com.io.lkconsultants.reverb.ReverbManager
import com.room.roomy.retrofit.RetrofitInstance
import com.room.roomy.retrofit.TokenProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed class UsersState {
    object Idle : UsersState()
    object Loading : UsersState()
    data class Success(val users: List<ConversationResponse>) : UsersState()
    data class Error(val message: String) : UsersState()
}

class UsersViewModel(app: Application) : AndroidViewModel(app) {

    private val gson = Gson()

    private val _state = MutableStateFlow<UsersState>(UsersState.Idle)
    val state: StateFlow<UsersState> = _state.asStateFlow()

    private val subscribed = mutableSetOf<Long>()

    init { fetchUsers() }

    fun fetchUsers() {
        viewModelScope.launch {
            _state.value = UsersState.Loading
            try {
                val response = RetrofitInstance.retrofits.getConversations()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        _state.value = UsersState.Success(body)
                        connectAndSubscribe(body)
                    } else {
                        _state.value = UsersState.Error("Empty response")
                    }
                } else {
                    _state.value = UsersState.Error("Failed: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _state.value = UsersState.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    private fun connectAndSubscribe(conversations: List<ConversationResponse>) {
        ReverbManager.connect {
            conversations.forEach { conv ->
                val id = conv.id.toLong()
                if (subscribed.contains(id)) return@forEach
                ReverbManager.subscribeConversation(id, listener)
                subscribed += id
            }
        }
    }

    private val listener = object : ChatChannelListener() {
        override fun onMessageSent(json: String) {
            try {
                val root = JSONObject(json)
                val payload = if (root.has("message")) root.getJSONObject("message").toString() else json
                val msg = gson.fromJson(payload, MessageResponse::class.java) ?: return
                handleIncoming(msg)
            } catch (e: Exception) {
                Log.e("UsersVM", "Parse MessageSent failed", e)
            }
        }
    }

    private fun handleIncoming(msg: MessageResponse) {
        val me = TokenProvider.getUserId().toIntOrNull() ?: -1
        val isMine = msg.sender?.id == me
        val current = _state.value as? UsersState.Success ?: return

        val updated = current.users.map { conv ->
            if (conv.id != msg.conversation_id) conv
            else conv.copy(
                last_message = msg.text ?: msg.file_name?.let { "File: $it" } ?: conv.last_message,
                updated_at = msg.created_at,
                unread_count = if (isMine || ChatNotifier.activeConversationId == conv.id) conv.unread_count
                               else conv.unread_count + 1
            )
        }.sortedByDescending { it.updated_at }

        _state.value = UsersState.Success(updated)
        // Notifications when backgrounded are posted by ChatService; when foregrounded,
        // the live badge bump is feedback enough — no need to interrupt the user.
    }

    /** Locally clear unread count when user opens a conversation. */
    fun clearUnread(conversationId: Int) {
        val current = _state.value as? UsersState.Success ?: return
        val updated = current.users.map {
            if (it.id == conversationId && it.unread_count > 0) it.copy(unread_count = 0) else it
        }
        _state.value = UsersState.Success(updated)
    }
}
