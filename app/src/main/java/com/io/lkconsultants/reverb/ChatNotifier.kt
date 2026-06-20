package com.io.lkconsultants.reverb

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.io.lkconsultants.MainActivity
import com.io.lkconsultants.R

/**
 * Foreground-aware chat notifier.
 *  - [setActive] / [clearActive] suppress notifications for the conversation the user is currently looking at.
 *  - [showNewMessage] posts a heads-up notification on the "chat_messages" channel.
 */
object ChatNotifier {

    private const val CHANNEL_ID = "chat_messages"
    private const val CHANNEL_NAME = "Messages"

    @Volatile
    var activeConversationId: Int = -1
        private set

    fun setActive(id: Int) { activeConversationId = id }
    fun clearActive() { activeConversationId = -1 }

    fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val ch = NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "New chat messages"
                    enableVibration(true)
                }
                nm.createNotificationChannel(ch)
            }
        }
    }

    fun showNewMessage(
        ctx: Context,
        conversationId: Int,
        senderName: String,
        body: String
    ) {
        if (conversationId == activeConversationId) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(ctx, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("conversationId", conversationId)
        }
        val pi = PendingIntent.getActivity(
            ctx, conversationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(senderName)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi)
            .build()

        NotificationManagerCompat.from(ctx).notify(conversationId, notif)
    }
}
