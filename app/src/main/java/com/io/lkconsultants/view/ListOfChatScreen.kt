package com.io.lkconsultants.view

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.io.lkconsultants.color.lkColors
import com.io.lkconsultants.model.ConversationResponse
import com.io.lkconsultants.ui.theme.Divider
import com.io.lkconsultants.view.LKColors.AccentBlue
import com.io.lkconsultants.view.LKColors.Divider
import com.io.lkconsultants.viewmodel.UsersState
import com.io.lkconsultants.viewmodel.UsersViewModel
import com.room.roomy.retrofit.TokenProvider
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UsersScreen(
    viewModel: UsersViewModel = viewModel(),
    onNewChat: () -> Unit = {},
    onLogout: () -> Unit = {},
    isShareMode: Boolean = false,
    onClick: (user: ConversationResponse) -> Unit
) {
    var selectedConversationId by remember { mutableStateOf<Int?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out?") },
            text = { Text("You'll need to sign in again to receive messages.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) { Text("Log out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = lkColors
    val presenceMap by com.io.lkconsultants.reverb.PresenceManager.statuses.collectAsStateWithLifecycle()

    // Ask for POST_NOTIFICATIONS once on Android 13+
    val notifLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { /* result ignored */ }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Refetch unread/last-message state every time the user returns to this screen.
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val obs = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.fetchUsers()
                com.io.lkconsultants.reverb.PresenceManager.refreshNow()
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    var searchOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val isRefreshing = state is UsersState.Loading
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        topBar = {
            Surface(shadowElevation = 4.dp, color = colors.surface) {
                Column {
                    TopAppBar(
                        title = { Text("Chats", color = colors.onSurface, fontWeight = FontWeight.Bold) },
                        actions = {
                            IconButton(onClick = {
                                searchOpen = !searchOpen
                                if (!searchOpen) query = ""
                            }) {
                                Icon(
                                    if (searchOpen) Icons.Default.Clear else Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = colors.primaryBlue
                                )
                            }
                            IconButton(onClick = onNewChat) {
                                Icon(Icons.Default.Add, contentDescription = "New chat", tint = colors.primaryBlue)
                            }
                            IconButton(onClick = { showLogoutDialog = true }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Log out",
                                    tint = colors.primaryBlue
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
                    )
                    if (searchOpen) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("Search chats…", color = colors.subtitle, fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = colors.primaryBlue) },
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primaryBlue,
                                unfocusedBorderColor = colors.divider,
                                focusedTextColor = colors.onSurface,
                                unfocusedTextColor = colors.onSurface,
                                cursorColor = colors.primaryBlue
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewChat,
                containerColor = colors.primaryBlue,
                contentColor = colors.white
            ) {
                Icon(Icons.Default.Add, contentDescription = "New chat")
            }
        },
        containerColor = colors.background
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                is UsersState.Idle -> Unit

                is UsersState.Loading -> Box(
                    Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = colors.primaryBlue) }

                is UsersState.Success -> {
                    val users = (state as UsersState.Success).users
                    val currentUserId = TokenProvider.getUserId()

                    val chatConversations = users.map { convo ->
                        convo.copy(
                            participants = convo.participants.filter { it.id != currentUserId.toInt() }
                        )
                    }

                    val q = query.trim().lowercase()
                    val filtered = if (q.isEmpty()) chatConversations
                                   else chatConversations.filter { c ->
                                       c.group_name?.lowercase()?.contains(q) == true ||
                                       c.last_message?.lowercase()?.contains(q) == true ||
                                       c.participants.any { it.name.lowercase().contains(q) }
                                   }

                    Log.d("Usss", users.toString())

                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.fetchUsers() },
                        modifier = Modifier.fillMaxSize(),
                        state = pullToRefreshState
                    ) {
                        if (filtered.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    if (q.isEmpty()) "No conversations yet" else "No matches",
                                    color = colors.subtitle
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().background(color = colors.background),
                                contentPadding = PaddingValues(3.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filtered) { user ->
                                    val otherId = user.participants.firstOrNull()?.id
                                    val isOnline = otherId != null && presenceMap[otherId]?.isOnline == true
                                    UserItemdd(
                                        user = user,
                                        unreadCount = user.unread_count,
                                        isOnline = isOnline,
                                        isSelected = isShareMode && selectedConversationId == user.id
                                    ) {
                                        if (isShareMode) {
                                            selectedConversationId = it.id
                                            // Optional: delay slightly so user sees the checkmark before navigation
                                            onClick.invoke(it)
                                        } else {
                                            viewModel.clearUnread(it.id)
                                            onClick.invoke(it)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                is UsersState.Error -> {
                    val message = (state as UsersState.Error).message
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = message, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.fetchUsers() }) { Text("Retry") }
                    }
                }
            }
        }
    }
}

//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//fun UsersScreen(
//    viewModel: UsersViewModel = viewModel(),
//    onClick: (user: ConversationResponse) -> Unit
//) {
//    val state by viewModel.state.collectAsStateWithLifecycle()
//
//    when (state) {
//        is UsersState.Idle -> Unit
//
//        is UsersState.Loading -> {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        }
//
//        is UsersState.Success -> {
//            val users = (state as UsersState.Success).users
//            LazyColumn(
//                modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars),
//                contentPadding = PaddingValues(3.dp),
//                verticalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                items(users) { user ->
//                    UserItemdd(user = user){
//                       onClick.invoke(it)
//                    }
//                }
//            }
//        }
//
//        is UsersState.Error -> {
//            val message = (state as UsersState.Error).message
//            Column(
//                modifier = Modifier.fillMaxSize(),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ) {
//                Text(text = message, color = MaterialTheme.colorScheme.error)
//                Spacer(modifier = Modifier.height(12.dp))
//                Button(onClick = { viewModel.fetchUsers() }) {
//                    Text("Retry")
//                }
//            }
//        }
//    }
//}






@RequiresApi(Build.VERSION_CODES.O)
fun getTimeFromUTC(utcTime: String): String {
    return try {
        val instant = Instant.parse(utcTime)

        val localDateTime = instant
            .atZone(ZoneId.systemDefault()) // ✅ now works
            .toLocalDateTime()

        val formatter = DateTimeFormatter.ofPattern("hh:mm a")

        formatter.format(localDateTime)

    } catch (e: Exception) {
        ""
    }
}



//@Composable
//fun UserItem(user: ConversationResponse) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(2.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(
//                text = user.group_name.toString(),
//                style = MaterialTheme.typography.titleMedium
//            )
//            Text(
//                text = user.last_message,
//                style = MaterialTheme.typography.titleMedium
//            )
////            Text(
////                text = user.isOnline.toString(),
////                style = MaterialTheme.typography.bodySmall,
////                color = MaterialTheme.colorScheme.onSurfaceVariant
////            )
//        }
//    }
//}


