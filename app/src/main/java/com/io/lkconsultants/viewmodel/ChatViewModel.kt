package com.io.lkconsultants.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.io.lkconsultants.reverb.ReverbManager
import com.room.roomy.retrofit.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatViewModel : ViewModel() {

    private val _state = MutableStateFlow<MessagesState>(MessagesState.Idle)
    val state: StateFlow<MessagesState> = _state.asStateFlow()

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages

    fun connect(userToken: String, conversationId: String) {
        val id = conversationId.toLong()   // make sure it's Long
        ReverbManager.connect(conversationId.toInt()) {
            //subscribe(id)
        }
    }
//    private fun subscribe(conversationId: Long) {
//
//        ReverbManager.subscribeConversation(
//            conversationId,
//            object : ChatChannelListener() {
//
//                override fun onSubscriptionSucceeded(channelName: String) {
//                    Log.d("VM", "✅ Subscribed: $channelName")
//                }
//
//                override fun onAuthenticationFailure(message: String, e: Exception?) {
//                    Log.e("VM", "❌ Auth failed: $message", e)
//                }
//
//                override fun onMessageSent(json: String) {
//                    Log.d("VM", "📩 RAW: $json")
//
//                    try {
//                        val obj = JSONObject(json)
//                        val msg = obj.getJSONObject("message")
//
//                        val text = msg.getString("text")
//                        val sender = msg.getJSONObject("sender").getString("name")
//
//                        val formatted = "$sender: $text"
//
//                        _messages.value = _messages.value + formatted
//
//                    } catch (e: Exception) {
//                        Log.e("VM", "Parse error", e)
//                    }
//                }
//
//                override fun onMessagesRead(json: String) {
//                    Log.d("VM", "👁 Read: $json")
//                }
//
//                override fun onUserTyping(json: String) {
//                    Log.d("VM", "⌨️ Typing: $json")
//                }
//            }
//        )
//    }

    override fun onCleared() {
        super.onCleared()
      //  ReverbManager.c
    }

    fun getMessages(conversationId: Int) {
        viewModelScope.launch {
            _state.value = MessagesState.Loading
            Log.d("Id",conversationId.toString())
            try {
                val response = RetrofitInstance.retrofits.getMessages(conversationId)
                Log.d("rrre",response.body().toString())

                if (response.isSuccessful && response.body() != null) {
                    _state.value = MessagesState.Success(response.body()!!)
                } else {
                    _state.value = MessagesState.Error("Failed: ${response.code()}")
                }

            } catch (e: Exception) {
                _state.value = MessagesState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    // OPTIONAL — reuse your file size logic for attachments
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
            bytes >= 1_024     -> "%.0f KB".format(bytes / 1_024.0)
            else               -> "$bytes B"
        }
    }
}