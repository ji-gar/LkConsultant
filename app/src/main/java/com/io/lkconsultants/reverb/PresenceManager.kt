package com.io.lkconsultants.reverb

import android.util.Log
import com.io.lkconsultants.ApplicationClass
import com.io.lkconsultants.model.UserStatus
import com.room.roomy.retrofit.RetrofitInstance
import com.room.roomy.retrofit.TokenProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Drives the user-presence feature:
 *  - heartbeat() → POST /status/heartbeat every 60 s while in foreground (matches the web frontend cadence).
 *  - poll()      → GET  /status/users every 30 s, exposed as a StateFlow for the UI.
 *
 * Started by ChatService so the same lifecycle that owns the websocket also owns presence.
 */
object PresenceManager {

    private const val TAG = "Presence"
    private const val HEARTBEAT_MS = 60_000L
    private const val POLL_MS = 30_000L

    private val _statuses = MutableStateFlow<Map<Int, UserStatus>>(emptyMap())
    val statuses: StateFlow<Map<Int, UserStatus>> = _statuses.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var heartbeatJob: Job? = null
    private var pollJob: Job? = null

    fun start() {
        if (heartbeatJob?.isActive != true) {
            heartbeatJob = scope.launch { heartbeatLoop() }
        }
        if (pollJob?.isActive != true) {
            pollJob = scope.launch { pollLoop() }
        }
    }

    fun stop() {
        heartbeatJob?.cancel(); heartbeatJob = null
        pollJob?.cancel(); pollJob = null
    }

    /** One-shot refresh — call when entering a screen that needs fresh status immediately. */
    fun refreshNow() {
        scope.launch { fetchOnce() }
    }

    fun statusOf(userId: Int): UserStatus? = _statuses.value[userId]

    private suspend fun heartbeatLoop() {
        while (true) {
            if (TokenProvider.getToken().isNotEmpty() && ApplicationClass.AppForegroundTracker.isForeground) {
                runCatching { RetrofitInstance.retrofits.heartbeat() }
                    .onFailure { Log.w(TAG, "heartbeat failed: ${it.message}") }
            }
            delay(HEARTBEAT_MS)
        }
    }

    private suspend fun pollLoop() {
        while (true) {
            if (TokenProvider.getToken().isNotEmpty()) fetchOnce()
            delay(POLL_MS)
        }
    }

    private suspend fun fetchOnce() {
        runCatching {
            val response = RetrofitInstance.retrofits.getUserStatuses()
            val list = response.body()?.users.orEmpty()
            _statuses.value = list.associateBy { it.id }
        }.onFailure { Log.w(TAG, "status poll failed: ${it.message}") }
    }
}
