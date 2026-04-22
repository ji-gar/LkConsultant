package com.io.lkconsultants.reverb

import android.util.Log
import com.pusher.client.ChannelAuthorizer
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.connection.*

import okhttp3.*


import com.pusher.client.channel.PrivateChannel

import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

object ReverbManager {

    private const val TAG = "Reverb"

    private const val REVERB_KEY = "ENmFzvymq1fPqNPieGBV"
    private const val REVERB_HOST = "ws-a192fe47-b57b-48ff-bfd7-4f574d381592-reverb.laravel.cloud"
    private const val REVERB_PORT = 443
    private const val API_BASE_URL = "https://lkedc.free.laravel.cloud/api"

    private lateinit var pusher: Pusher
    private var isConnected = false
    private var authToken: String = ""

    // Store active channels (IMPORTANT)
    private val channels = ConcurrentHashMap<String, PrivateChannel>()

    // =========================
    // CONNECT
    // =========================
    fun connect(token: String, onConnected: (() -> Unit)? = null) {

        if (isConnected) return
        authToken = token

        val options = PusherOptions().apply {
            setHost(REVERB_HOST)
            setWsPort(REVERB_PORT)
            setWssPort(REVERB_PORT)
            isUseTLS = true

            channelAuthorizer = ChannelAuthorizer { channelName, socketId ->
                authorizeChannel(channelName, socketId)
            }
        }

        pusher = Pusher(REVERB_KEY, options)

        pusher.connect(object : ConnectionEventListener {

            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Log.d(TAG, "STATE: ${change.currentState}")

                if (change.currentState == ConnectionState.CONNECTED) {
                    isConnected = true
                    Log.d(TAG, "✅ CONNECTED SUCCESS")
                    onConnected?.invoke()
                }
            }

            override fun onError(message: String, code: String?, e: Exception?) {
                Log.e(TAG, "❌ ERROR: $message code=$code", e)
            }

        }, ConnectionState.ALL)
    }

    // =========================
    // SUBSCRIBE CHAT
    // =========================
    fun subscribeConversation(
        conversationId: String,
        onMessage: (String) -> Unit
    ) {

        val channelName = "private-conversation.$conversationId"

        if (channels.containsKey(channelName)) {
            Log.d(TAG, "Already subscribed: $channelName")
            return
        }

        val channel = pusher.subscribePrivate(channelName,
            object : PrivateChannelEventListener {

                override fun onSubscriptionSucceeded(channelName: String) {
                    Log.d(TAG, "✅ SUBSCRIBED: $channelName")
                }

                override fun onEvent(event: PusherEvent) {

                    Log.d(TAG, "EVENT: ${event.eventName}")
                    Log.d(TAG, "DATA: ${event.data}")

                    when (event.eventName) {

                        // ✅ IMPORTANT (dot prefix)
                        ".MessageSent" -> {
                            onMessage(event.data)
                        }

                        ".MessagesRead" -> {

                            onMessage(event.data)
                            // handle read receipts if needed
                        }
                    }
                }

                override fun onAuthenticationFailure(message: String, e: Exception?) {
                    Log.e(TAG, "❌ AUTH FAILED: $message", e)
                }
            })

        channels[channelName] = channel
    }

    // =========================
    // UNSUBSCRIBE
    // =========================
    fun unsubscribeConversation(conversationId: String) {
        val channelName = "private-conversation.$conversationId"

        try {
            pusher.unsubscribe(channelName)
            channels.remove(channelName)
            Log.d(TAG, "❌ UNSUBSCRIBED: $channelName")
        } catch (e: Exception) {
            Log.e(TAG, "Unsubscribe error", e)
        }
    }

    // =========================
    // DISCONNECT
    // =========================
    fun disconnect() {
        try {
            channels.keys.forEach { pusher.unsubscribe(it) }
            channels.clear()
            pusher.disconnect()
        } catch (_: Exception) {
        }

        isConnected = false
        Log.d(TAG, "Disconnected")
    }

    // =========================
    // AUTH (FIXED PROPERLY)
    // =========================
    private fun authorizeChannel(
        channelName: String,
        socketId: String
    ): String {

        val client = OkHttpClient()

        val body = FormBody.Builder()
            .add("socket_id", socketId)
            .add("channel_name", channelName)
            .build()

        val request = Request.Builder()
            .url("$API_BASE_URL/broadcasting/auth")
            .post(body)
            .addHeader("Authorization", "Bearer $authToken")
            .addHeader("Accept", "application/json")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .build()

        client.newCall(request).execute().use { response ->

            val result = response.body?.string()

            Log.d(TAG, "AUTH RESPONSE: $result")

            if (!response.isSuccessful) {
                throw IOException("Auth failed: ${response.code}")
            }

            return result ?: throw IOException("Empty auth response")
        }
    }
}

//object ReverbManager {
//
//    private const val REVERB_KEY = "ENmFzvymq1fPqNPieGBV"
//    private const val REVERB_HOST = "ws-a192fe47-b57b-48ff-bfd7-4f574d381592-reverb.laravel.cloud"
//    private const val REVERB_PORT = 443
//
//    // ⚠️ IMPORTANT → Replace with your Laravel API domain
//    private const val API_BASE_URL = "https://lkedc.free.laravel.cloud/api"
//
//    private lateinit var pusher: Pusher
//    private var isConnected = false
//    private var isSubscribed = false
//
//    // =========================
//    // CONNECT
//    // =========================
//    fun connect(authToken: String) {
//
//        if (isConnected) return
//        isConnected = true
//
//        val options = PusherOptions().apply {
//            setHost(REVERB_HOST)
//
//            // ✅ FIXED (important for wss)
//            setWsPort(REVERB_PORT)
//            setWssPort(REVERB_PORT)
//            isUseTLS = true
//
//            // ✅ AUTH FIX
//            channelAuthorizer = object : ChannelAuthorizer {
//                override fun authorize(channelName: String, socketId: String): String {
//                    return authorizeChannel(channelName, socketId, authToken)
//                }
//            }
//        }
//
//        pusher = Pusher(REVERB_KEY, options)
//
//        pusher.connect(object : ConnectionEventListener {
//
//            override fun onConnectionStateChange(change: ConnectionStateChange) {
//                Log.d("Reverb", "State: ${change.currentState}")
//            }
//
//            override fun onError(message: String, code: String?, e: Exception?) {
//                Log.e("Reverb", "Error: $message code=$code", e)
//            }
//
//        }, ConnectionState.ALL)
//    }
//
//    // =========================
//    // CHAT LISTENER
//    // =========================
//    fun listenToChat(conversationId: String, onMessage: (String) -> Unit) {
//
//        val channelName = "private-conversation.$conversationId"
//
//        // ✅ Unsubscribe previous (VERY IMPORTANT)
//        try {
//            pusher.unsubscribe(channelName)
//        } catch (e: Exception) {
//            Log.e("Reverb", "Unsubscribe error", e)
//        }
//
//        pusher.subscribePrivate(channelName, object : PrivateChannelEventListener {
//
//            override fun onSubscriptionSucceeded(channelName: String) {
//                Log.d("Reverb", "Subscribed: $channelName")
//            }
//
//            override fun onEvent(event: PusherEvent) {
//
//                Log.d("Reverb", "EVENT NAME: ${event.eventName}")
//                Log.d("Reverb", "EVENT DATA: ${event.data}")
//
//                // ✅ TEMP: DON'T FILTER (debug first)
//                onMessage(event.data)
//
//                // AFTER CONFIRM → use this:
//                // if (event.eventName == "MessageSent") {
//                //     onMessage(event.data)
//                // }
//            }
//
//            override fun onAuthenticationFailure(message: String, e: Exception?) {
//                Log.e("Reverb", "Auth failed: $message", e)
//            }
//        })
//    }
//
//    // =========================
//    // NOTIFICATION LISTENER
//    // =========================
//
//    // =========================
//    // DISCONNECT
//    // =========================
//    fun disconnect() {
//        isConnected = false
//        isSubscribed = false
//        pusher.disconnect()
//    }
//
//    // =========================
//    // AUTHORIZATION (FIXED)
//    // =========================
//    private fun authorizeChannel(
//        channelName: String,
//        socketId: String,
//        authToken: String
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
//            // ✅ FIXED → API DOMAIN (NOT WS HOST)
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
//            Log.d("Reverb", "Auth response: $result")
//
////            if (response.isSuccessful) {
////                throw IOException("Auth failed: ${response.code}")
////            }
//
//            return result ?: throw IOException("Auth response empty")
//        }
//    }
//}



