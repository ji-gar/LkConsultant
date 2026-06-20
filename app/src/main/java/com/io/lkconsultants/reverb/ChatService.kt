package com.io.lkconsultants.reverb

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.io.lkconsultants.ApplicationClass
import com.io.lkconsultants.MainActivity
import com.io.lkconsultants.R
import com.io.lkconsultants.model.MessageResponse
import com.room.roomy.retrofit.RetrofitInstance
import com.room.roomy.retrofit.TokenProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Foreground service that keeps the Reverb websocket alive even when no Activity is running.
 * Subscribes to every conversation the user belongs to and posts a system notification
 * whenever a new message arrives in a chat the user is not currently viewing.
 *
 * Caveat: a *force-stopped* / swipe-killed app stops this service too. True
 * "always-deliver" notifications require server push (FCM).
 */
class ChatService : Service() {

    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val subscribed = mutableSetOf<Long>()
    private var bootstrapJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ChatNotifier.ensureChannel(this)
        ensureForegroundChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(SERVICE_NOTIFICATION_ID, buildPersistentNotification())

        if (TokenProvider.getToken().isEmpty()) {
            Log.w(TAG, "No auth token; stopping service")
            stopSelf()
            return START_NOT_STICKY
        }

        PresenceManager.start()

        bootstrapJob?.cancel()
        bootstrapJob = scope.launch {
            try {
                val response = RetrofitInstance.retrofits.getConversations()
                val conversations = response.body().orEmpty()
                ReverbManager.connect {
                    conversations.forEach { conv ->
                        val id = conv.id.toLong()
                        if (subscribed.add(id)) {
                            ReverbManager.subscribeConversation(id, listener)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "bootstrap failed", e)
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.coroutineContext[Job]?.cancel()
        subscribed.forEach { ReverbManager.removeConversationListener(it, listener) }
        subscribed.clear()
        PresenceManager.stop()
    }

    private val listener = object : ChatChannelListener() {
        override fun onMessageSent(json: String) {
            try {
                val root = JSONObject(json)
                val payload = if (root.has("message")) root.getJSONObject("message").toString() else json
                val msg = gson.fromJson(payload, MessageResponse::class.java) ?: return
                val me = TokenProvider.getUserId().toIntOrNull() ?: -1
                if (msg.sender?.id == me) return
                // While any Activity is in the foreground, the in-app UI handles updates.
                // Only post a system notification when the app is in the background.
                if (ApplicationClass.AppForegroundTracker.isForeground) return

                val sender = msg.sender?.name ?: "New message"
                val body = msg.text ?: msg.file_name?.let { "Sent a file: $it" } ?: "New message"
                ChatNotifier.showNewMessage(applicationContext, msg.conversation_id, sender, body)
            } catch (e: Exception) {
                Log.e(TAG, "MessageSent parse failed", e)
            }
        }
    }

    private fun ensureForegroundChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java) ?: return
            if (nm.getNotificationChannel(FG_CHANNEL_ID) == null) {
                val ch = NotificationChannel(
                    FG_CHANNEL_ID, "Chat connection", NotificationManager.IMPORTANCE_MIN
                ).apply { description = "Keeps you connected to chat" }
                nm.createNotificationChannel(ch)
            }
        }
    }

    private fun buildPersistentNotification(): Notification {
        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, FG_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("LK Consultants")
            .setContentText("Connected to chat")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(pi)
            .build()
    }

    companion object {
        private const val TAG = "ChatService"
        private const val FG_CHANNEL_ID = "chat_connection"
        private const val SERVICE_NOTIFICATION_ID = 1001

        fun start(ctx: Context) {
            val intent = Intent(ctx, ChatService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, ChatService::class.java))
        }
    }
}
