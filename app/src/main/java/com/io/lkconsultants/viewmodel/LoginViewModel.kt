package com.io.lkconsultants.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.io.lkconsultants.model.LoginRequest
import com.io.lkconsultants.model.User
import com.room.roomy.retrofit.RetrofitInstance
import com.room.roomy.retrofit.TokenProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = LoginState.Error("Email and password are required")
            return
        }

        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                val response = RetrofitInstance.retrofits.login(LoginRequest(email, password))
                when {
                    response.isSuccessful -> {
                        val body = response.body()!!
                        TokenProvider.setToken(body.token)
                        TokenProvider.setUserId(body.user.id.toString())
                        _state.value = LoginState.Success(body.user)
                    }
                    response.code() == 401 -> {
                        _state.value = LoginState.Error("Invalid email or password")
                    }
                    response.code() == 403 -> {
                        _state.value = LoginState.Error("Your account is inactive")
                    }
                    else -> {
                        _state.value = LoginState.Error("Login failed. Try again.")
                    }
                }
            } catch (e: Exception) {
                _state.value = LoginState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}