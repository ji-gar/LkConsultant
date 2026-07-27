package com.io.lkconsultants.reverb

import android.util.Log
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PrivateChannel
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpChannelAuthorizer
import com.room.roomy.retrofit.TokenProvider
import java.util.concurrent.ConcurrentHashMap

/**
 * Wrapper around Pusher Java client targeting Laravel Reverb.
 * Mirrors the reference realtime-chat-kotlin module:
 *  - separate connect() and subscribeConversation()
 *  - tracks subscribed channels for clean teardown
 *  - exposes connection state
 */
object ReverbManager {

    private const val TAG = "Reverb"

    private const val KEY = "ENmFzvymq1fPqNPieGBV"
    private const val HOST = "ws-a192fe47-b57b-48ff-bfd7-4f574d381592-reverb.laravel.cloud"
    private const val PORT = 443
    private const val AUTH_URL = "https://lkedc.laravel.cloud/api/broadcasting/auth"

    enum class State { IDLE, CONNECTING, CONNECTED, DISCONNECTED, FAILED }

    private var pusher: Pusher? = null
    private val channels = ConcurrentHashMap<String, PrivateChannel>()
    private val listeners = ConcurrentHashMap<Long, MutableSet<PrivateChannelEventListener>>()
    private val stateListeners = mutableSetOf<(State) -> Unit>()

    @Volatile
    var state: State = State.IDLE
        private set

    val socketId: String? get() = pusher?.connection?.socketId

    fun addStateListener(l: (State) -> Unit) {
        stateListeners += l
        l(state)
    }

    fun removeStateListener(l: (State) -> Unit) {
        stateListeners -= l
    }

    private fun notifyState(s: State) {
        state = s
        stateListeners.toList().forEach { it(s) }
    }

    fun connect(onReady: (() -> Unit)? = null) {
        if (state == State.CONNECTED) {
            onReady?.invoke()
            return
        }
        if (state == State.CONNECTING) return

        val token = TokenProvider.getToken()
        if (token.isEmpty()) {
            Log.e(TAG, "No auth token; aborting connect")
            notifyState(State.FAILED)
            return
        }

        val authorizer = HttpChannelAuthorizer(AUTH_URL).apply {
            setHeaders(
                mapOf(
                    "Authorization" to "Bearer $token",
                    "Accept" to "application/json"
                )
            )
        }

        val options = PusherOptions().apply {
            setHost(HOST)
            setWssPort(PORT)
            isUseTLS = true
            setChannelAuthorizer(authorizer)
            setActivityTimeout(30_000)
        }

        notifyState(State.CONNECTING)

        pusher = Pusher(KEY, options).also { p ->
            p.connect(object : ConnectionEventListener {
                override fun onConnectionStateChange(c: ConnectionStateChange) {
                    Log.d(TAG, "STATE ${c.previousState} -> ${c.currentState}")
                    when (c.currentState) {
                        ConnectionState.CONNECTED -> {
                            notifyState(State.CONNECTED)
                            onReady?.invoke()
                        }
                        ConnectionState.CONNECTING,
                        ConnectionState.RECONNECTING -> notifyState(State.CONNECTING)
                        ConnectionState.DISCONNECTED -> notifyState(State.DISCONNECTED)
                        else -> {}
                    }
                }

                override fun onError(message: String?, code: String?, e: Exception?) {
                    Log.e(TAG, "ERROR $code: $message", e)
                    notifyState(State.FAILED)
                }
            }, ConnectionState.ALL)
        }
    }

    fun subscribeConversation(
        conversationId: Long,
        listener: PrivateChannelEventListener
    ): PrivateChannel? {
        val set = listeners.getOrPut(conversationId) {
            java.util.Collections.synchronizedSet(mutableSetOf())
        }
        set += listener

        val p = pusher ?: run {
            Log.e(TAG, "subscribe before connect()")
            return null
        }
        val name = "private-conversation.$conversationId"
        channels[name]?.let { return it }

        val dispatcher = object : PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent) {
                snapshot(conversationId).forEach { runCatching { it.onEvent(event) } }
            }
            override fun onSubscriptionSucceeded(channelName: String?) {
                snapshot(conversationId).forEach { runCatching { it.onSubscriptionSucceeded(channelName) } }
            }
            override fun onAuthenticationFailure(message: String?, e: Exception?) {
                snapshot(conversationId).forEach { runCatching { it.onAuthenticationFailure(message, e) } }
            }
        }
        val channel = p.subscribePrivate(
            name,
            dispatcher,
            "MessageSent", "MessagesRead", "UserTyping"
        )
        channels[name] = channel
        return channel
    }

    private fun snapshot(id: Long): List<PrivateChannelEventListener> =
        listeners[id]?.let { synchronized(it) { it.toList() } } ?: emptyList()

    /** Remove a single listener; tears down the underlying channel only if no listeners remain. */
    fun removeConversationListener(conversationId: Long, listener: PrivateChannelEventListener) {
        val set = listeners[conversationId] ?: return
        synchronized(set) { set.remove(listener) }
        if (synchronized(set) { set.isEmpty() }) {
            unsubscribe(conversationId)
            listeners.remove(conversationId)
        }
    }

    fun unsubscribe(conversationId: Long) {
        val name = "private-conversation.$conversationId"
        pusher?.unsubscribe(name)
        channels.remove(name)
    }

    fun disconnect() {
        channels.keys.forEach { pusher?.unsubscribe(it) }
        channels.clear()
        pusher?.disconnect()
        pusher = null
        notifyState(State.IDLE)
    }
}

abstract class ChatChannelListener : PrivateChannelEventListener {
    abstract fun onMessageSent(json: String)
    open fun onMessagesRead(json: String) {}
    open fun onUserTyping(json: String) {}

    override fun onEvent(event: PusherEvent) {
        Log.d("Reverb", "EVENT ${event.eventName} -> ${event.data}")
        when (event.eventName) {
            "MessageSent" -> onMessageSent(event.data)
            "MessagesRead" -> onMessagesRead(event.data)
            "UserTyping" -> onUserTyping(event.data)
        }
    }

    override fun onSubscriptionSucceeded(channelName: String) {
        Log.d("Reverb", "SUBSCRIBED $channelName")
    }

    override fun onAuthenticationFailure(message: String?, e: Exception?) {
        Log.e("Reverb", "AUTH FAILED $message", e)
    }
}
