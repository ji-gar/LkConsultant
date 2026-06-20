package com.io.lkconsultants.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.io.lkconsultants.model.MarkReadRequest
import com.io.lkconsultants.model.Message
import com.io.lkconsultants.model.MessageResponse
import com.io.lkconsultants.reverb.ChatChannelListener
import com.io.lkconsultants.reverb.ReverbManager
import com.room.roomy.retrofit.RetrofitInstance
import com.room.roomy.retrofit.TokenProvider
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatViewModel : ViewModel() {

    private val gson = Gson()

    private val _state = MutableStateFlow<MessagesState>(MessagesState.Idle)
    val state: StateFlow<MessagesState> = _state.asStateFlow()

    private val _connection = MutableStateFlow(ReverbManager.State.IDLE)
    val connection: StateFlow<ReverbManager.State> = _connection.asStateFlow()

    private var subscribedId: Long = -1L
    private val stateListener: (ReverbManager.State) -> Unit = { _connection.value = it }

    fun connect(userToken: String, conversationId: String) {
        val id = conversationId.toLong()
        subscribedId = id
        ReverbManager.addStateListener(stateListener)
        ReverbManager.connect {
            ReverbManager.subscribeConversation(id, channelListener)
        }
    }

    private val channelListener = object : ChatChannelListener() {
        override fun onMessageSent(json: String) {
            try {
                val root = JSONObject(json)
                // Laravel broadcastWith typically wraps payload as {"message": {...}}
                val payload = if (root.has("message")) root.getJSONObject("message").toString()
                              else json
                val msg = gson.fromJson(payload, MessageResponse::class.java) ?: return
                appendRealtime(msg)
                // Incoming message in an open chat → mark conversation as read.
                val me = TokenProvider.getUserId().toIntOrNull() ?: -1
                if (msg.sender.id != me) markRead(msg.conversation_id)
            } catch (e: Exception) {
                Log.e("ChatVM", "Parse MessageSent failed", e)
            }
        }

        override fun onMessagesRead(json: String) {
            try {
                val o = JSONObject(json)
                val convId = o.optInt("conversation_id", -1)
                val readerId = o.optInt("user_id", -1)
                val readAtIso = o.optString("read_at", "")
                val me = TokenProvider.getUserId().toIntOrNull() ?: -1
                if (readerId == me || readerId == -1 || readAtIso.isBlank()) return
                applyReadReceipt(convId, readAtIso)
            } catch (e: Exception) {
                Log.e("ChatVM", "Parse MessagesRead failed", e)
            }
        }
    }

    private fun applyReadReceipt(conversationId: Int, readAtIso: String) {
        val readAtMs = parseIso(readAtIso) ?: return
        val current = _state.value as? MessagesState.Success ?: return
        val me = TokenProvider.getUserId().toIntOrNull() ?: -1
        var changed = false
        val updated = current.data.messages.map { m ->
            if (!m.read_by_all &&
                m.conversation_id == conversationId &&
                m.sender.id == me &&
                (parseIso(m.created_at) ?: Long.MAX_VALUE) <= readAtMs
            ) {
                changed = true
                m.copy(read_by_all = true)
            } else m
        }
        if (changed) _state.value = MessagesState.Success(current.data.copy(messages = updated))
    }

    private fun parseIso(iso: String): Long? {
        if (iso.isBlank()) return null
        val normalized = iso
            .replace(Regex("(\\.\\d{3})\\d+"), "$1")
            .replace("Z", "+0000")
            .replace(Regex("([+-]\\d{2}):(\\d{2})$"), "$1$2")
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (p in patterns) {
            try {
                val sdf = SimpleDateFormat(p, Locale.US)
                if (p == "yyyy-MM-dd HH:mm:ss") sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(normalized)?.time
            } catch (_: Exception) { }
        }
        return null
    }

    fun markRead(conversationId: Int) {
        viewModelScope.launch {
            runCatching {
                RetrofitInstance.retrofits.markRead(MarkReadRequest(conversationId))
            }.onFailure { Log.e("ChatVM", "markRead failed", it) }
        }
    }

    private fun appendRealtime(msg: MessageResponse) {
        val current = _state.value
        if (current is MessagesState.Success) {
            val existing = current.data.messages

            if (existing.any { it.id == msg.id }) return
            val updated = current.data.copy(messages = existing + msg)

            _state.value = MessagesState.Success(updated)
        } else {
            _state.value = MessagesState.Success(Message(messages = listOf(msg)))
        }
    }

    /** Optimistic append for the message we just sent ourselves. */
    fun appendLocal(msg: MessageResponse) = appendRealtime(msg)

    override fun onCleared() {
        super.onCleared()
        ReverbManager.removeStateListener(stateListener)
        if (subscribedId > 0) ReverbManager.removeConversationListener(subscribedId, channelListener)
    }

    fun getMessages(conversationId: Int) {
        viewModelScope.launch {
            _state.value = MessagesState.Loading
            try {
                val response = RetrofitInstance.retrofits.getMessages(conversationId)
                if (response.isSuccessful && response.body() != null) {
                    _state.value = MessagesState.Success(response.body()!!)
                    markRead(conversationId)
                } else {
                    _state.value = MessagesState.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = MessagesState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun formatFileSize(bytes: Long): String = when {
        bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
        bytes >= 1_024 -> "%.0f KB".format(bytes / 1_024.0)
        else -> "$bytes B"
    }
}
