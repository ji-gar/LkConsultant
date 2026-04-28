package com.io.lkconsultants.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.io.lkconsultants.model.MessageWrapper

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
        ReverbManager.connect(conversationId.toInt()) {

            val json = Gson().fromJson(it, MessageWrapper::class.java)

            val newMessage = json.message

            val currentList = (_state.value as? MessagesState.Success)?.messages ?: emptyList()

            _state.value = MessagesState.Success(
                currentList + newMessage   // ✅ append
            )
        }
    }

//    fun connect(userToken: String, conversationId: String) {
//        val id = conversationId.toLong()   // make sure it's Long
//        ReverbManager.connect(conversationId.toInt()) {
//            val data = it   // or it.getData()
//            val json = Gson().fromJson(data, MessageWrapper::class.java)
//
//
//
//
//           _state.value = MessagesState.SingleMessage(json.message)
//        }
//    }


    override fun onCleared() {
        super.onCleared()
      //  ReverbManager.c
    }

    fun getMessages(conversationId: Int) {
        viewModelScope.launch {
            _state.value = MessagesState.Loading

            try {
                val response = RetrofitInstance.retrofits.getMessages(conversationId)

                if (response.isSuccessful && response.body() != null) {

                    val list = response.body()!!.messages   // ✅ extract list

                    _state.value = MessagesState.Success(list)

                } else {
                    _state.value = MessagesState.Error("Failed: ${response.code()}")
                }

            } catch (e: Exception) {
                _state.value = MessagesState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

//    fun getMessages(conversationId: Int) {
//        viewModelScope.launch {
//            _state.value = MessagesState.Loading
//            Log.d("Id",conversationId.toString())
//            try {
//                val response = RetrofitInstance.retrofits.getMessages(conversationId)
//                Log.d("rrre",response.body().toString())
//
//                if (response.isSuccessful && response.body() != null) {
//                    _state.value = MessagesState.Success(response.body()!!)
//                } else {
//                    _state.value = MessagesState.Error("Failed: ${response.code()}")
//                }
//
//            } catch (e: Exception) {
//                _state.value = MessagesState.Error("Network error: ${e.localizedMessage}")
//            }
//        }
//    }

    // OPTIONAL — reuse your file size logic for attachments
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
            bytes >= 1_024     -> "%.0f KB".format(bytes / 1_024.0)
            else               -> "$bytes B"
        }
    }
}