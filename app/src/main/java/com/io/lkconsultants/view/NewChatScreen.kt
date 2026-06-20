package com.io.lkconsultants.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.io.lkconsultants.color.lkColors
import com.io.lkconsultants.model.ChatUser
import com.io.lkconsultants.viewmodel.CreateConversationState
import com.io.lkconsultants.viewmodel.NewChatState
import com.io.lkconsultants.viewmodel.NewChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatScreen(
    viewModel: NewChatViewModel = viewModel(),
    onBack: () -> Unit,
    onConversationReady: (conversationId: Int, participantId: Int, name: String) -> Unit
) {
    val colors = lkColors
    val state by viewModel.state.collectAsStateWithLifecycle()
    val createState by viewModel.createState.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }

    LaunchedEffect(createState) {
        val s = createState
        if (s is CreateConversationState.Success) {
            onConversationReady(s.conversation.id, s.target.id, s.target.name)
            viewModel.resetCreateState()
        }
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp, color = colors.surface) {
                TopAppBar(
                    title = { Text("New Chat", color = colors.onSurface, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.primaryBlue)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
                )
            }
        },
        containerColor = colors.background
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search users…", color = colors.subtitle, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.primaryBlue) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = colors.subtitle)
                        }
                    }
                },
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

            when (val s = state) {
                NewChatState.Idle -> Unit
                NewChatState.Loading -> Box(
                    Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = colors.primaryBlue) }

                is NewChatState.Error -> Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(s.message, color = colors.brandRed)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.fetchUsers() },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBlue)
                    ) { Text("Retry", color = colors.white) }
                }

                is NewChatState.Success -> {
                    val q = query.trim().lowercase()
                    val filtered = if (q.isEmpty()) s.users
                                   else s.users.filter {
                                       it.name.lowercase().contains(q) || it.email.lowercase().contains(q)
                                   }

                    if (filtered.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No users found", color = colors.subtitle)
                        }
                    } else {
                        val isCreating = createState is CreateConversationState.Loading
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(filtered, key = { it.id }) { user ->
                                NewChatUserRow(
                                    user = user,
                                    enabled = !isCreating,
                                    onClick = { viewModel.startConversationWith(user) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewChatUserRow(user: ChatUser, enabled: Boolean, onClick: () -> Unit) {
    val colors = lkColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(colors.primaryBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = colors.primaryBlue,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(user.name, color = colors.onSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "${user.role.replaceFirstChar { it.uppercase() }} • ${user.email}",
                color = colors.subtitle, fontSize = 11.sp
            )
        }
    }
}
