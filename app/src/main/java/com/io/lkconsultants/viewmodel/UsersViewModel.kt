package com.io.lkconsultants.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.io.lkconsultants.model.ConversationResponse
import com.io.lkconsultants.model.User
import com.io.lkconsultants.model.UserStatus
import com.room.roomy.retrofit.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UsersState {
    object Idle : UsersState()
    object Loading : UsersState()
    data class Success(val users: List<ConversationResponse>) : UsersState()
    data class Error(val message: String) : UsersState()
}

class UsersViewModel : ViewModel() {

    private val _state = MutableStateFlow<UsersState>(UsersState.Idle)
    val state: StateFlow<UsersState> = _state.asStateFlow()

    init {
        fetchUsers()
    }

    fun fetchUsers() {
        viewModelScope.launch {
            _state.value = UsersState.Loading

            try {
                val response = RetrofitInstance.retrofits.getConversations()

                Log.d("Res", response.body().toString())

                if (response.isSuccessful) {

                    val body = response.body()

                    if (body != null) {
                        _state.value = UsersState.Success(body)
                    } else {
                        _state.value = UsersState.Error("Empty response")
                    }

                } else {
                    _state.value = UsersState.Error(
                        "Failed: ${response.code()} - ${response.message()}"
                    )
                }

            } catch (e: Exception) {
                _state.value = UsersState.Error(
                    e.localizedMessage ?: "Network error"
                )
            }
        }
    }
}