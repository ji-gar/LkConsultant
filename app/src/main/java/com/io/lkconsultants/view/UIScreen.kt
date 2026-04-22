package com.io.lkconsultants.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.io.lkconsultants.model.ConversationResponse

/**
 * A Material You / Google Messages–style conversation list item.
 *
 * Features:
 *  - Gradient avatar with per-name colour seeding
 *  - Unread badge with animated dot + bold last-message text
 *  - Press-scale micro-interaction
 *  - Ripple-less tonal press highlight (matches M3 NavigationDrawerItem behaviour)
 *  - Subtle divider inset-aligned to content
 *
 * @param user            The conversation data to display.
 * @param unreadCount     Number of unread messages (0 = read state).
 * @param onClick         Called when the item is tapped.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserItemdd(
    user: ConversationResponse,
    unreadCount: Int = 0,
    onClick: (ConversationResponse) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Subtle press-scale feedback
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "press_scale"
    )

    // Tonal surface highlight on press (M3 style, no ripple jar)
    val bgColor by animateColorAsState(
        targetValue = if (isPressed)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        else
            Color.Transparent,
        animationSpec = tween(120),
        label = "press_bg"
    )

    val hasUnread = unreadCount > 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .background(bgColor, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null          // handled by bgColor above
            ) { onClick(user) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ── Avatar ────────────────────────────────────────────────────────
            if (!user.group_name.isNullOrEmpty())
            {
                val initials = buildInitials(user.group_name)
                val avatarGradient = rememberAvatarGradient(user.group_name)

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(avatarGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
            else {
                val initials = buildInitials(user.participants.get(0).name)
                val avatarGradient = rememberAvatarGradient(user.participants.get(0).name)

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(avatarGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }



            Spacer(modifier = Modifier.width(14.dp))

            // ── Content ───────────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {

                // Row 1: Name + timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.group_name ?: "${user.participants.get(0).name}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = (-0.2).sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = getTimeFromUTC(user.updated_at),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (hasUnread)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal
                        )
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Row 2: Last message + unread badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.last_message ?: "No messages yet",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (hasUnread)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (hasUnread) {
                        Spacer(modifier = Modifier.width(8.dp))
                        UnreadBadge(count = unreadCount)
                    }
                }
            }
        }

        // Inset divider aligned to text content (skips avatar + padding)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 82.dp, end = 16.dp)
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        )
    }
}

// ── Unread badge ─────────────────────────────────────────────────────────────

@Composable
private fun UnreadBadge(count: Int) {
    val label = if (count > 99) "99+" else count.toString()
    val isWide = count > 9

    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = if (isWide) 6.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Returns up to 2 initials from the group/contact name. */
private fun buildInitials(name: String?): String {
    if (name.isNullOrBlank()) return "?"
    val parts = name.trim().split(" ")
    return if (parts.size >= 2) {
        "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
    } else {
        parts[0].take(2).uppercase()
    }
}

/**
 * Deterministic avatar gradient seeded from the name's hash.
 * Cycles through a curated palette of Google-style gradient pairs.
 */
@Composable
private fun rememberAvatarGradient(name: String?): Brush {
    val palettes = listOf(
        Pair(Color(0xFF4285F4), Color(0xFF0F9D58)), // Google Blue → Green
        Pair(Color(0xFFEA4335), Color(0xFFFBBC05)), // Red → Yellow
        Pair(Color(0xFF9C27B0), Color(0xFF3F51B5)), // Purple → Indigo
        Pair(Color(0xFF00897B), Color(0xFF1976D2)), // Teal → Blue
        Pair(Color(0xFFFF7043), Color(0xFFFFCA28)), // Deep Orange → Amber
        Pair(Color(0xFF5C6BC0), Color(0xFF26C6DA)), // Indigo → Cyan
        Pair(Color(0xFF43A047), Color(0xFF00ACC1)), // Green → Light Blue
        Pair(Color(0xFFE91E63), Color(0xFFFF5722)), // Pink → Deep Orange
    )
    val index = (name?.hashCode()?.and(0x7FFFFFFF) ?: 0) % palettes.size
    val (start, end) = palettes[index]
    return Brush.linearGradient(colors = listOf(start, end))
}