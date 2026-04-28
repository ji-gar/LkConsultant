package com.io.lkconsultants.reverb

import android.util.Log
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.Channel
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent


import com.pusher.client.channel.PrivateChannel

import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpChannelAuthorizer
import com.room.roomy.retrofit.TokenProvider
import org.json.JSONObject

import java.util.concurrent.ConcurrentHashMap


object ReverbManager {

    private const val KEY = "ENmFzvymq1fPqNPieGBV"
    private const val HOST = "ws-a192fe47-b57b-48ff-bfd7-4f574d381592-reverb.laravel.cloud"

    //auth
    private const val AUTH_URL =
        "https://lkedc.free.laravel.cloud/api/broadcasting/auth"

    private var pusher: Pusher? = null

    fun connect(id: Int, onMessage: (String) -> Unit) {

        val options = PusherOptions().apply {
            setHost(HOST)
            setWssPort(443)
            isUseTLS = true

            val authorizer = HttpChannelAuthorizer(AUTH_URL)
            authorizer.setHeaders(
                mapOf(
                    "Authorization" to "Bearer ${TokenProvider.getToken()}",
                    "Accept" to "application/json"
                )
            )
            setChannelAuthorizer(authorizer)
        }

        pusher = Pusher(KEY, options)
        pusher?.connect()


        pusher?.subscribePrivate(
            "private-conversation.$id",
            object : PrivateChannelEventListener {

                override fun onEvent(event: PusherEvent) {
                    Log.d("REVERB", "EVENT = ${event.eventName}")
                    Log.d("REVERB", "DATA = ${event.data}")

                    if (event.eventName.contains("MessageSent")) {
                        onMessage(event.data)
                    }
                }

                override fun onSubscriptionSucceeded(channelName: String) {
                    Log.d("REVERB", "SUBSCRIBED: $channelName")
                }

                override fun onAuthenticationFailure(message: String?, e: Exception?) {
                    Log.e("REVERB", "AUTH FAILED: $message", e)
                }

            },
            "MessageSent"
        )
    }

    fun disconnect() {
        pusher?.disconnect()
        pusher = null
    }
}
//today morning 28-04-2026

//object ReverbManager {
//    private const val TAG = "Reverb"
//
//    // ---- CONFIG ----
//    private const val KEY = "ENmFzvymq1fPqNPieGBV"
//    private const val HOST = "ws-a192fe47-b57b-48ff-bfd7-4f574d381592-reverb.laravel.cloud"
//    private const val PORT = 443
//    private const val AUTH_URL = "https://lkedc.free.laravel.cloud/broadcasting/auth"
//    private var pusher: Pusher? = null
//    private var channel: Channel? = null
//
//    fun connect(id:Int,onMessage: (String) -> Unit) {
//
//        val options = PusherOptions().apply {
//            setHost(HOST)
//            setWssPort(443)
//            isUseTLS = true
//
//            val authorizer = HttpChannelAuthorizer(AUTH_URL)
//            authorizer.setHeaders(
//                mapOf("Authorization" to "Bearer ${TokenProvider.getToken()}")
//            )
//            setChannelAuthorizer(authorizer)
//        }
//
//        val pusher = Pusher(KEY, options)
//
//        pusher.connect()
//
//        val channel = pusher.subscribePrivate(
//            "private-conversation.$id",
//            object : PrivateChannelEventListener {
//
//                override fun onEvent(event: PusherEvent) {
//                    Log.d("Reverb", "🔥 EVENT: ${event.eventName}")
//                    Log.d("Reverb", "📦 DATA: ${event.data}")
//
//                    if (event.eventName == "MessageSent") {
//                        Log.d("Reverb", "MESSAGE: ${event.data}")
//                    }
//                }
//
//                override fun onSubscriptionSucceeded(channelName: String) {
//                    Log.d("Reverb", "✅ Subscribed: $channelName")
//                }
//
//                override fun onAuthenticationFailure(message: String?, e: Exception?) {
//                    Log.e("Reverb", "❌ AUTH FAILED: $message", e)
//                }
//            },
//            "MessageSent"
//        )
//
//
//    }
//
//    fun disconnect() {
//        pusher?.disconnect()
//    }
//}


//object ReverbManager {
//
//    private const val TAG = "Reverb"
//
//    // ---- CONFIG ----
//    private const val KEY = "ENmFzvymq1fPqNPieGBV"
//    private const val HOST = "ws-a192fe47-b57b-48ff-bfd7-4f574d381592-reverb.laravel.cloud"
//    private const val PORT = 443
//    private const val AUTH_URL = "https://lkedc.free.laravel.cloud/broadcasting/auth"
//
//    enum class State { IDLE, CONNECTING, CONNECTED, DISCONNECTED, FAILED }
//
//    private var pusher: Pusher? = null
//    private val channels = ConcurrentHashMap<String, PrivateChannel>()
//
//    @Volatile
//    var state: State = State.IDLE
//        private set
//
//    val socketId: String?
//        get() = pusher?.connection?.socketId
//
//    // ================= CONNECT =================
//    fun connect(onReady: (() -> Unit)? = null) {
//
//        if (state == State.CONNECTED) {
//            onReady?.invoke()
//            return
//        }
//
//        val token = TokenProvider.getToken()
//
//        val authorizer = HttpChannelAuthorizer(AUTH_URL).apply {
//            setHeaders(
//                mapOf(
//                    "Authorization" to "Bearer $token",
//                    "Accept" to "application/json"
//                )
//            )
//        }
//
//        val options = PusherOptions().apply {
//            setHost(HOST)
//            setWssPort(PORT)
//            isUseTLS = true
//            setChannelAuthorizer(authorizer)
//            setActivityTimeout(30000)
//        }
//
//        state = State.CONNECTING
//
//        pusher = Pusher(KEY, options)
//
//        pusher?.connect(object : ConnectionEventListener {
//
//            override fun onConnectionStateChange(change: ConnectionStateChange) {
//                Log.d(TAG, "STATE: ${change.currentState}")
//
//                when (change.currentState) {
//                    ConnectionState.CONNECTED -> {
//                        state = State.CONNECTED
//                        Log.d(TAG, "✅ CONNECTED socketId=${socketId}")
//                        onReady?.invoke()
//                    }
//
//                    ConnectionState.CONNECTING,
//                    ConnectionState.RECONNECTING -> {
//                        state = State.CONNECTING
//                    }
//
//                    ConnectionState.DISCONNECTED -> {
//                        state = State.DISCONNECTED
//                    }
//
//                    else -> {}
//                }
//            }
//
//            override fun onError(message: String?, code: String?, e: Exception?) {
//                Log.e(TAG, "❌ ERROR: $message", e)
//                state = State.FAILED
//            }
//
//        }, ConnectionState.ALL)
//    }
//
//    // ================= SUBSCRIBE =================
//    fun subscribeConversation(
//        conversationId: Long,
//        listener: ChatChannelListener
//    ): PrivateChannel? {
//
//        val p = pusher ?: return null
//
//        val channelName = "private-conversation.$conversationId"
//
//        // Already subscribed
//        channels[channelName]?.let { return it }
//
//        Log.d(TAG, "📡 Subscribing to $channelName")
//
//        val channel = p.subscribePrivate(
//            channelName,
//            listener,
//
//            // ✅ CORRECT EVENT NAMES
//            "App\\Events\\MessageSent",
//            "App\\Events\\MessagesRead",
//            "App\\Events\\UserTyping"
//        )
//
//        channels[channelName] = channel
//        return channel
//    }
//
//    fun unsubscribe(conversationId: Long) {
//        val name = "private-conversation.$conversationId"
//        pusher?.unsubscribe(name)
//        channels.remove(name)
//    }
//
//    fun disconnect() {
//        channels.keys.forEach { pusher?.unsubscribe(it) }
//        channels.clear()
//        pusher?.disconnect()
//        pusher = null
//        state = State.IDLE
//    }
//}

/**
 * Convenience listener that decodes the JSON event payload for you.
 */
//abstract class ChatChannelListener : PrivateChannelEventListener {
//
//    abstract fun onMessageSent(message: JSONObject)
//    abstract fun onMessagesRead(data: JSONObject)
//    abstract fun onUserTyping(data: JSONObject)
//
//    override fun onEvent(event: PusherEvent) {
//
//        Log.d("Reverb", "🔥 EVENT: ${event.eventName}")
//        Log.d("Reverb", "📦 DATA: ${event.data}")
//
//        try {
//            val json = JSONObject(event.data)
//
//            when (event.eventName) {
//
//                // ✅ FIXED EVENT NAMES
//                "App\\Events\\MessageSent" -> {
//                    val message = json.getJSONObject("message")
//                    onMessageSent(message)
//                }
//
//                "App\\Events\\MessagesRead" -> {
//                    onMessagesRead(json)
//                }
//
//                "App\\Events\\UserTyping" -> {
//                    onUserTyping(json)
//                }
//            }
//
//        } catch (e: Exception) {
//            Log.e("Reverb", "❌ JSON PARSE ERROR", e)
//        }
//    }
//
//    override fun onSubscriptionSucceeded(channelName: String) {
//        Log.d("Reverb", "✅ Subscribed: $channelName")
//    }
//
//    override fun onAuthenticationFailure(message: String?, e: Exception?) {
//        Log.e("Reverb", "❌ AUTH FAILED: $message", e)
//    }
//}
//object ReverbManager {
//
//    private const val TAG = "Reverb"
//
//    private const val REVERB_KEY = "ENmFzvymq1fPqNPieGBV"
//    private const val REVERB_HOST = "ws-a192fe47-b57b-48ff-bfd7-4f574d381592-reverb.laravel.cloud"
//    private const val REVERB_PORT = 443
//    private const val API_BASE_URL = "https://lkedc.free.laravel.cloud/api"
//
//    private lateinit var pusher: Pusher
//    private var isConnected = false
//    private var authToken: String = ""
//
//    // Store active channels (IMPORTANT)
//    private val channels = ConcurrentHashMap<String, PrivateChannel>()
//
//    // =========================
//    // CONNECT
//    // =========================
//    fun connect(token: String, onConnected: (() -> Unit)? = null) {
//
//        if (isConnected) return
//        authToken = token
//
//        val options = PusherOptions().apply {
//            setHost(REVERB_HOST)
//            setWsPort(REVERB_PORT)
//            setWssPort(REVERB_PORT)
//            isUseTLS = true
//
//            channelAuthorizer = ChannelAuthorizer { channelName, socketId ->
//                authorizeChannel(channelName, socketId)
//            }
//        }
//
//        pusher = Pusher(REVERB_KEY, options)
//
//        pusher.connect(object : ConnectionEventListener {
//
//            override fun onConnectionStateChange(change: ConnectionStateChange) {
//                Log.d(TAG, "STATE: ${change.currentState}")
//
//                if (change.currentState == ConnectionState.CONNECTED) {
//                    isConnected = true
//
//                    Log.d(TAG, "✅ CONNECTED SUCCESS")
//                    onConnected?.invoke()
//                }
//            }
//
//            override fun onError(message: String, code: String?, e: Exception?) {
//                Log.e(TAG, "❌ ERROR: $message code=$code", e)
//            }
//
//        }, ConnectionState.ALL)
//    }
//
//    // =========================
//    // SUBSCRIBE CHAT
//    // =========================
//    fun subscribeConversation(
//        conversationId: String,
//        onMessage: (String) -> Unit
//    ) {
//
//        val channelName = "private-conversation.$conversationId"
//
//        if (channels.containsKey(channelName)) {
//            Log.d(TAG, "Already subscribed: $channelName")
//            return
//        }
//
//
//
//        val channel = pusher.subscribePrivate(channelName,
//            object : PrivateChannelEventListener {
//
//                override fun onSubscriptionSucceeded(channelName: String) {
//                    Log.d(TAG, "✅ SUBSCRIBED: $channelName")
//                }
//
//                override fun onEvent(event: PusherEvent) {
//
//                    Log.d(TAG, "EVENT: ${event.eventName}")
//                    Log.d(TAG, "DATA: ${event.data}")
//
//                    when (event.eventName) {
//
//                        // ✅ IMPORTANT (dot prefix)
//                        ".MessageSent" -> {
//                            onMessage(event.data)
//                        }
//
//                        ".MessagesRead" -> {
//
//                            onMessage(event.data)
//                            // handle read receipts if needed
//                        }
//                    }
//                }
//
//                override fun onAuthenticationFailure(message: String, e: Exception?) {
//                    Log.e(TAG, "❌ AUTH FAILED: $message", e)
//                }
//            })
//
//        channels[channelName] = channel
//    }
//
//    // =========================
//    // UNSUBSCRIBE
//    // =========================
//    fun unsubscribeConversation(conversationId: String) {
//        val channelName = "private-conversation.$conversationId"
//
//        try {
//            pusher.unsubscribe(channelName)
//            channels.remove(channelName)
//            Log.d(TAG, "❌ UNSUBSCRIBED: $channelName")
//        } catch (e: Exception) {
//            Log.e(TAG, "Unsubscribe error", e)
//        }
//    }
//
//    // =========================
//    // DISCONNECT
//    // =========================
//    fun disconnect() {
//        try {
//            channels.keys.forEach { pusher.unsubscribe(it) }
//            channels.clear()
//            pusher.disconnect()
//        } catch (_: Exception) {
//        }
//
//        isConnected = false
//        Log.d(TAG, "Disconnected")
//    }
//
//    // =========================
//    // AUTH (FIXED PROPERLY)
//    // =========================
//    private fun authorizeChannel(
//        channelName: String,
//        socketId: String
//    ): String {
//
//        val client = OkHttpClient()
//
//        val body = FormBody.Builder()
//            .add("socket_id", socketId)
//            .add("channel_name", channelName)
//            .build()
//
//        val request = Request.Builder()
//            .url("$API_BASE_URL/broadcasting/auth")
//            .post(body)
//            .addHeader("Authorization", "Bearer $authToken")
//            .addHeader("Accept", "application/json")
//            .addHeader("X-Requested-With", "XMLHttpRequest")
//            .build()
//
//        client.newCall(request).execute().use { response ->
//
//            val result = response.body?.string()
//
//            Log.d(TAG, "AUTH RESPONSE: $result")
//
//            if (!response.isSuccessful) {
//                throw IOException("Auth failed: ${response.code}")
//            }
//
//            return result ?: throw IOException("Empty auth response")
//        }
//    }
//}

