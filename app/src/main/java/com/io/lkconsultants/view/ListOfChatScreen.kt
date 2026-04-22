package com.io.lkconsultants.view

import android.os.Build
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.io.lkconsultants.model.ConversationResponse
import com.io.lkconsultants.ui.theme.Divider
import com.io.lkconsultants.view.LKColors.AccentBlue
import com.io.lkconsultants.view.LKColors.Divider
import com.io.lkconsultants.viewmodel.UsersState
import com.io.lkconsultants.viewmodel.UsersViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UsersScreen(
    viewModel: UsersViewModel = viewModel(),
    onClick: (user: ConversationResponse) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // ✅ Track refresh state
    val isRefreshing = state is UsersState.Loading

    // ✅ PullToRefresh state
    val pullToRefreshState = rememberPullToRefreshState()

    when (state) {
        is UsersState.Idle -> Unit

        is UsersState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is UsersState.Success -> {
            val users = (state as UsersState.Success).users

            // ✅ Wrap with PullToRefreshBox
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.fetchUsers() }, // ✅ Trigger refresh
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars),
                state = pullToRefreshState
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(3.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users) { user ->
                        UserItemdd(user = user) {
                            onClick.invoke(it)
                        }
                    }
                }
            }
        }

        is UsersState.Error -> {
            val message = (state as UsersState.Error).message
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = message, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { viewModel.fetchUsers() }) {
                    Text("Retry")
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


