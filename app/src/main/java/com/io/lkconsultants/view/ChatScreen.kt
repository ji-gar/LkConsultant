package com.io.lkconsultants.view

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.io.lkconsultants.view.LKColors.White
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.io.lkconsultants.model.ApiSharedFile
import com.io.lkconsultants.viewmodel.ChatViewModel
import com.io.lkconsultants.viewmodel.FilesViewModel
import com.io.lkconsultants.viewmodel.MessagesState
import com.io.lkconsultants.viewmodel.SendMessageState
import com.io.lkconsultants.viewmodel.SendMessageViewModel
import com.room.roomy.retrofit.TokenProvider
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


object LKColors {
    val PrimaryBlue     = Color(0xFF1565C0)   // deep blue "LK" letters
    val PrimaryBlueDark = Color(0xFF0D47A1)
    val AccentBlue      = Color(0xFF29B6F6)   // diamond shape lighter blue
    val AccentBlueMid   = Color(0xFF1976D2)
    val BrandRed        = Color(0xFFD32F2F)   // "DESIGN CONSULTANT" text
    val BrandRedLight   = Color(0xFFEF5350)
    val White           = Color(0xFFFFFFFF)
    val Background      = Color(0xFFF0F4FF)
    val Surface         = Color(0xFFFFFFFF)
    val OnSurface       = Color(0xFF0D1B2A)
    val Subtitle        = Color(0xFF546E7A)
    val Divider         = Color(0xFFCFD8DC)
    val ChatBubbleAdmin = Color(0xFF1565C0)
    val ChatBubbleUser  = Color(0xFFE3F2FD)
    val FileCardBg      = Color(0xFFF5F9FF)
    val BottomBar       = Color(0xFF0D47A1)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: FilesViewModel = viewModel(), fileView: ChatViewModel =viewModel(),sendMessageViewModel: SendMessageViewModel=viewModel(), id:Int,name:String,onBack:()-> Unit) {
    val context   = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var rember    by remember { mutableStateOf<Long?>(null) }

    // ✅ Collect the sealed state
    //val state by viewModel.state.collectAsStateWithLifecycle()
    val state1 by fileView.state.collectAsStateWithLifecycle()
    val sendMessageState = sendMessageViewModel.state.collectAsStateWithLifecycle().value






    // Call once (like onCreate)
    LaunchedEffect(id) {
        val userToken = TokenProvider.getToken()
        val conversationId = id
        fileView.connect(userToken, conversationId.toString())
    }
    LaunchedEffect(sendMessageState) {
        if (sendMessageState is SendMessageState.Success) {
            inputText = ""                    // clear the input field
            fileView.getMessages(id)          // re-fetch messages from API
            sendMessageViewModel.resetState() // reset so it doesn't re-trigger
        }
    }

    // ✅ Trigger load on first composition

    LaunchedEffect(Unit) { fileView.getMessages(id) }


    val apiFiles1  = (state1 as? MessagesState.Success)?.data?.messages ?: emptyList()
    val isLoading1 = state1 is MessagesState.Loading
    val error1    = (state1 as? MessagesState.Error)?.message

    // Map API files → ChatMessage list
    val messages=apiFiles1

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }



    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(listOf(LKColors.AccentBlue, LKColors.PrimaryBlue))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                              //  Text("${"${messages.get(0).sender.name}"}", color = White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    "${"${name}"}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LKColors.OnSurface
                                )
                                Text(
                                    when (state1) {
                                        is MessagesState.Loading -> "Loading..."
                                        is MessagesState.Success -> "Super Admin • ${apiFiles1.size} files"
                                        is MessagesState.Error   -> "Error loading"
                                        else                  -> ""
                                    },
                                    fontSize = 11.sp,
                                    color = LKColors.Subtitle
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            onBack.invoke()
                        }) {
                            Icon(Icons.Default.ArrowBack, null, tint = LKColors.PrimaryBlue)
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) { Icon(Icons.Default.Search, null, tint = LKColors.PrimaryBlue) }
                        IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, null, tint = LKColors.PrimaryBlue) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = LKColors.Surface)
                )
            }
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = LKColors.Surface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.DateRange, null, tint = LKColors.PrimaryBlue)
                    }
                    OutlinedTextField(
                        value         = inputText,
                        onValueChange = { inputText = it },
                        placeholder   = { Text("Type a message...", color = LKColors.Subtitle, fontSize = 14.sp) },
                        modifier      = Modifier.weight(1f),
                        shape         = RoundedCornerShape(24.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = LKColors.PrimaryBlue,
                            unfocusedBorderColor = LKColors.Divider
                        ),
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    val isLoading = sendMessageState is SendMessageState.Loading

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(LKColors.PrimaryBlue)
                            .clickable(enabled = !isLoading) {
                                sendMessageViewModel.sendMessage(id, inputText)
                            },
                        contentAlignment = Alignment.Center
                    ) {

                        if (isLoading) {
                            CircularProgressIndicator(
                                color = White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                tint = White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

//                    when (sendMessage) {
//                        is SendMessageState.Loading -> {
//                            Box(
//                                modifier = Modifier
//                                    .size(46.dp)
//                                    .clip(CircleShape)
//                                    .background(LKColors.PrimaryBlue)
//                                    .clickable {
//
//                                        sendMessageViewModel.sendMessage(id,inputText)
//                                    },
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Log.d("dddd","dddd")
//                                CircularProgressIndicator()
//                            }
//                        }
//
//                        is SendMessageState.Error   -> {
//                            Log.d("dddd","eeeee")
//                        }
//                        else                  -> "Super Admin"
//                    }
//                    Box(
//                        modifier = Modifier
//                            .size(46.dp)
//                            .clip(CircleShape)
//                            .background(LKColors.PrimaryBlue)
//                            .clickable {
//                             Log.d("xxxx","ddd")
//                                sendMessageViewModel.sendMessage(id,inputText)
//                            },
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Icon(Icons.Default.Send, null, tint = White, modifier = Modifier.size(20.dp))
//                    }
                }
            }
        },
        containerColor = LKColors.Background
    ) { padding ->

        if (isLoading1) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = LKColors.PrimaryBlue)
            }
            return@Scaffold
        }

        // ✅ Error state with retry button
        if (error1 != null) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error1, color = LKColors.BrandRed)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.getFiles() }) {
                        Text("Retry")
                    }
                }
            }
            return@Scaffold
        }

        // ✅ Success state — show messages
        LazyColumn(
            state               = listState,
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding      = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Surface(shape = RoundedCornerShape(12.dp), color = LKColors.Divider) {
                        Text(
                            "Today",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = LKColors.Subtitle,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            items(messages) { message ->


                if (message.file_name!=null)
                {

                    FileBubble(
                        ChatMessage.FileMsg(
                            file = SharedFile(
                                1,
                                file_name = message.file_name,
                                file_url = message.file_url,
                                size = "10",
                                sender = message.sender.name,
                                type = getFileExtension(message.file_name.toString())!!,
                                time = formatTime(message.created_at),
                                message = ""
                            ), message.id.toString().equals(TokenProvider.getUserId().toString()), time = formatTime(message.created_at)
                        ),
                        onDownload = {
                            rember=  donwload(context,message.file_url.toString(),message.file_name)
                        }
                    ) {
                        if ( rember!= null) {
                                val uri = getDownloadedFileUri(context, rember!!)

                                if (uri != null) {
                                    shareFile(context, uri)
                                } else {
                                    Toast.makeText(context, "File not downloaded yet", Toast.LENGTH_SHORT).show()
                                }

                            } else {
                                Toast.makeText(context, "Please download first", Toast.LENGTH_SHORT).show()
                            }

                    }
                }
                else {
                    TextBubble(ChatMessage.TextMsg(message.text.toString(), isMine = message.sender.id.toString().equals(TokenProvider.getUserId().toString()),formatTime(message.created_at)))
                }
            }
        }
    }
}

fun getFileExtension(fileName: String?): String? {
    return fileName
        ?.substringAfterLast('.', "")
        ?.takeIf { it.isNotEmpty() }
}


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ChatScreen() {
//    val context = LocalContext.current
//    var inputText by remember { mutableStateOf("") }
//    var showShareDialog by remember { mutableStateOf(false) }
//    var selectedFile by remember { mutableStateOf<SharedFile?>(null) }
//    var rember by remember { mutableStateOf<Long?>(null) }
//
//    val sampleFiles = listOf(
//        SharedFile(1, "LK_Project_Blueprint.pdf", "pdf",    "4.2 MB",  "Super Admin", "9:00 AM",  "Final blueprint for client review"),
//        SharedFile(2, "Site_Plan_v3.dwg",         "dwg",    "12.7 MB", "Super Admin", "9:15 AM",  "Updated site plan with revisions"),
//        SharedFile(3, "Interior_Mood_Board.jpg",  "image",  "8.1 MB",  "Super Admin", "9:30 AM",  "Mood board for interior design"),
//        SharedFile(4, "Project_Timeline.docx",    "doc",    "1.3 MB",  "Super Admin", "10:00 AM", "Timeline & milestones document"),
//        SharedFile(5, "3D_Renders_Pack.zip",      "zip",    "67.4 MB", "Super Admin", "10:30 AM", "All 3D render files compressed"),
//    )
//
//    val messages: List<ChatMessage> = listOf(
//        ChatMessage.TextMsg("Good morning team! Sharing today's project files below.", true, "9:00 AM"),
//        ChatMessage.FileMsg(sampleFiles[0], true, "9:01 AM"),
//        ChatMessage.FileMsg(sampleFiles[1], true, "9:15 AM"),
//        ChatMessage.TextMsg("Please review the blueprint and site plan before the meeting.", true, "9:16 AM"),
//        ChatMessage.TextMsg("Received! Will go through them shortly.", false, "9:20 AM"),
//        ChatMessage.FileMsg(sampleFiles[2], true, "9:30 AM"),
//        ChatMessage.TextMsg("Also sharing the interior mood board for reference.", true, "9:31 AM"),
//        ChatMessage.TextMsg("The mood board looks great! Love the color palette.", false, "9:45 AM"),
//        ChatMessage.FileMsg(sampleFiles[3], true, "10:00 AM"),
//        ChatMessage.FileMsg(sampleFiles[4], true, "10:30 AM"),
//        ChatMessage.TextMsg("All files uploaded. Please download and review before EOD.", true, "10:31 AM"),
//    )
//
//    val listState = rememberLazyListState()
//    LaunchedEffect(Unit) { listState.animateScrollToItem(messages.size - 1) }
//
//    Scaffold(
//        topBar = {
//            Surface(shadowElevation = 4.dp) {
//                TopAppBar(
//                    title = {
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Box(
//                                modifier = Modifier
//                                    .size(40.dp)
//                                    .clip(CircleShape)
//                                    .background(
//                                        Brush.radialGradient(
//                                            listOf(LKColors.AccentBlue, LKColors.PrimaryBlue)
//                                        )
//                                    ),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text("LK", color =White,
//                                    fontSize = 14.sp, fontWeight = FontWeight.Black)
//                            }
//                            Spacer(Modifier.width(10.dp))
//                            Column {
//                                Text("LK Project Channel",
//                                    fontSize = 15.sp, fontWeight = FontWeight.Bold,
//                                    color = LKColors.OnSurface)
//                                Text("Super Admin • 12 members",
//                                    fontSize = 11.sp, color = LKColors.Subtitle)
//                            }
//                        }
//                    },
//                    navigationIcon = {
//                        IconButton(onClick = { }) {
//                            Icon(Icons.Default.ArrowBack, null, tint = LKColors.PrimaryBlue)
//                        }
//                    },
//                    actions = {
//                        IconButton(onClick = {}) {
//                            Icon(Icons.Default.Search, null, tint = LKColors.PrimaryBlue)
//                        }
//                        IconButton(onClick = {}) {
//                            Icon(Icons.Default.MoreVert, null, tint = LKColors.PrimaryBlue)
//                        }
//                    },
//                    colors = TopAppBarDefaults.topAppBarColors(containerColor = LKColors.Surface)
//                )
//            }
//        },
//        bottomBar = {
//            Surface(shadowElevation = 8.dp, color = LKColors.Surface) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .navigationBarsPadding()
//                        .padding(horizontal = 12.dp, vertical = 10.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    IconButton(onClick = {}) {
//                        Icon(Icons.Default.DateRange, null, tint = LKColors.PrimaryBlue)
//                    }
//                    OutlinedTextField(
//                        value         = inputText,
//                        onValueChange = { inputText = it },
//                        placeholder   = { Text("Type a message...", color = LKColors.Subtitle,
//                            fontSize = 14.sp) },
//                        modifier      = Modifier.weight(1f),
//                        shape         = RoundedCornerShape(24.dp),
//                        colors        = OutlinedTextFieldDefaults.colors(
//                            focusedBorderColor   = LKColors.PrimaryBlue,
//                            unfocusedBorderColor = LKColors.Divider
//                        ),
//                        singleLine    = true
//                    )
//                    Spacer(Modifier.width(8.dp))
//                    Box(
//                        modifier = Modifier
//                            .size(46.dp)
//                            .clip(CircleShape)
//                            .background(LKColors.PrimaryBlue)
//                            .clickable { },
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Icon(Icons.Default.Send, null,
//                            tint = LKColors.White, modifier = Modifier.size(20.dp))
//                    }
//                }
//            }
//        },
//        containerColor = LKColors.Background
//    ) { padding ->
//        LazyColumn(
//            state           = listState,
//            modifier        = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .padding(horizontal = 12.dp),
//            verticalArrangement = Arrangement.spacedBy(6.dp),
//            contentPadding  = PaddingValues(vertical = 12.dp)
//        ) {
//            // Date header
//            item {
//                Row(
//                    Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.Center
//                ) {
//                    Surface(
//                        shape = RoundedCornerShape(12.dp),
//                        color = LKColors.Divider
//                    ) {
//                        Text("Today", modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
//                            fontSize = 11.sp, color = LKColors.Subtitle, fontWeight = FontWeight.Medium)
//                    }
//                }
//                Spacer(Modifier.height(4.dp))
//            }
//
//            items(messages) { message ->
//                when (message) {
//                    is ChatMessage.TextMsg -> TextBubble(message)
//                    is ChatMessage.FileMsg -> FileBubble(
//                        message = message,
//                        onDownload = {
//
//
//                           // Toast.makeText(context, "Download started: $rember", Toast.LENGTH_SHORT).show()
//                       //  var d=  downloadFile(context,"https://www.iitk.ac.in/esc101/share/downloads/javanotes5.pdf","dddd.pdf")
//
//                          rember=  donwload(context,"https://developers.google.com/kml/documentation/KML_Samples.kml")
//
//                            //Log.d("dddddd",getDownloadStatus(context,d))
//                        },
//                        onShare = {
//                            if ( rember!= null) {
//                                val uri = getDownloadedFileUri(context, rember!!)
//
//                                if (uri != null) {
//                                    shareFile(context, uri)
//                                } else {
//                                    Toast.makeText(context, "File not downloaded yet", Toast.LENGTH_SHORT).show()
//                                }
//
//                            } else {
//                                Toast.makeText(context, "Please download first", Toast.LENGTH_SHORT).show()
//                            }
//
//                        }
//                    )
//                }
//            }
//        }
//    }
//}

fun downloadFile(
    context: Context,
    url: String,
    fileName: String
): Long {

    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    val request = DownloadManager.Request(Uri.parse(url)).apply {

        setTitle(fileName)

        setDescription("Downloading...")

        setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        )

        setRequiresCharging(false)

        setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_WIFI or
                    DownloadManager.Request.NETWORK_MOBILE
        )

        setAllowedOverMetered(true)
        setAllowedOverRoaming(true)

        setMimeType("*/*")

        // ✅ IMPORTANT CHANGE (Android 16 safe)
        setDestinationInExternalFilesDir(
            context,
            Environment.DIRECTORY_DOWNLOADS,
            "${"ddddd"}_$fileName"
        )
    }

    return dm.enqueue(request)
}



// Your existing local model (keep as-is)
data class SharedFile(
    val id: Int,
    val file_name: String,
    val file_url: String?,
    val type: String,        // derived from mime_type or file extension
    val size: String,        // formatted from file_size bytes
    val sender: String,      // from shared_by.name
    val time: String,        // formatted from created_at
    val message: String?     // from message field
)

// Extension to convert API model → local model
fun ApiSharedFile.toLocalSharedFile(): SharedFile {
    return SharedFile(
        id = this.id,
        file_name = this.file_name,
        file_url =this.file_url,
        type = getFileType(this.file_name),   // helper below
        size = formatSize(this.file_size),     // helper below
        sender = this.shared_by.name,
        time = formatTime(this.created_at),    // helper below
        message = this.message
    )
}

// Helpers
fun getFileType(fileName: String): String {
    return when (fileName.substringAfterLast('.').lowercase()) {
        "pdf"              -> "pdf"
        "jpg", "jpeg",
        "png", "gif",
        "webp"             -> "image"
        "doc", "docx"      -> "doc"
        "xls", "xlsx"      -> "xls"
        "zip", "rar"       -> "zip"
        "dwg"              -> "dwg"
        "txt"              -> "txt"
        else               -> "file"
    }
}

fun formatSize(bytes: Long): String = when {
    bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1_024     -> "%.0f KB".format(bytes / 1_024.0)
    else               -> "$bytes B"
}

fun formatTime(isoDate: String): String {
    return try {
        val sdf    = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date   = sdf.parse(isoDate)
        val out    = SimpleDateFormat("hh:mm a", Locale.getDefault())
        out.format(date!!)
    } catch (e: Exception) { isoDate }
}



fun donwload(context: Context, url: String, fileName: String) : Long
{
Log.d("fff",url.toString())
    var request= DownloadManager.Request(Uri.parse(url))
        .setTitle("${fileName}")
        .setNotificationVisibility( DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setNotificationVisibility( DownloadManager.Request.NETWORK_WIFI or
                DownloadManager.Request.NETWORK_MOBILE)


   var downloadManeger= context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
   return downloadManeger.enqueue(request)


}





fun shareFile(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "*/*" // change if needed (e.g., application/pdf)
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Share File"))
}

fun getDownloadedFileUri(context: Context, downloadId: Long): Uri? {
    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    return dm.getUriForDownloadedFile(downloadId)
}


// ─────────────────────────────────────────────
//  TEXT BUBBLE
// ─────────────────────────────────────────────
@Composable
fun TextBubble(message: ChatMessage.TextMsg) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMine) Arrangement.Start else Arrangement.End
    ) {
        if (message.isMine) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(LKColors.PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {  }
            Spacer(Modifier.width(6.dp))
        }

        Column(horizontalAlignment = if (message.isMine) Alignment.Start else Alignment.End) {
            if (message.isMine) {

            }
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (message.isMine) 4.dp else 18.dp,
                    topEnd   = if (message.isMine) 18.dp else 4.dp,
                    bottomStart = 18.dp, bottomEnd = 18.dp
                ),
                color = if (message.isMine) LKColors.ChatBubbleAdmin else LKColors.ChatBubbleUser,
                shadowElevation = 1.dp
            ) {
                Text(
                    text     = message.text,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color    = if (message.isMine) White else LKColors.OnSurface,
                    fontSize = 14.sp
                )
            }
            Text(message.time, fontSize = 10.sp, color = LKColors.Subtitle,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp))
        }
    }
}

// ─────────────────────────────────────────────
//  FILE BUBBLE
// ─────────────────────────────────────────────
@Composable
fun FileBubble(message: ChatMessage.FileMsg, onDownload:()->Unit,onShare: () -> Unit) {
    val file = message.file
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(LKColors.PrimaryBlue),
            contentAlignment = Alignment.Center
        ) {  }
        Spacer(Modifier.width(6.dp))

        Column {
            Text("Super Admin", fontSize = 10.sp, color = LKColors.PrimaryBlue,
                fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 2.dp))

            Surface(
                modifier = Modifier.widthIn(max = 300.dp),
                shape    = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp,
                    bottomStart = 18.dp, bottomEnd = 18.dp),
                color    = LKColors.Surface,
                shadowElevation = 2.dp,
                border   = BorderStroke(1.dp, LKColors.Divider)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // File icon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(fileIconBg(file.type)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text     = file.type.uppercase(),
                                color    = White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(file.file_name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                color = LKColors.OnSurface, maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                            Text(file.size, fontSize = 11.sp, color = LKColors.Subtitle)
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = LKColors.Divider)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Download
                        OutlinedButton(
                            onClick  = {

                                onDownload.invoke()
                            },
                            modifier = Modifier.weight(1f).height(34.dp),
                            shape    = RoundedCornerShape(8.dp),
                            border   = BorderStroke(1.dp, LKColors.PrimaryBlue),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, null,
                                tint = LKColors.PrimaryBlue, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Download", color = LKColors.PrimaryBlue,
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        // Share
                        Button(
                            onClick  = onShare,
                            modifier = Modifier.weight(1f).height(34.dp),
                            shape    = RoundedCornerShape(8.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = LKColors.PrimaryBlue),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Share, null,
                                tint = White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Share", color = White,
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Text(message.time, fontSize = 10.sp, color = LKColors.Subtitle,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp))
        }
    }
}


/*data class SharedFile(
    val id: Int,
    val name: String,
    val type: String,        // "pdf","image","dwg","doc","zip"
    val size: String,
    val uploadedBy: String,
    val uploadedAt: String,
    val description: String
)*/

sealed class ChatMessage {
    data class TextMsg(val text: String, val isMine: Boolean, val time: String) : ChatMessage()
    data class FileMsg(val file: SharedFile, val isMine: Boolean, val time: String) : ChatMessage()
}

fun fileIconBg(type: String): Color = when (type) {
    "pdf"   -> Color(0xFFD32F2F)
    "dwg"   -> Color(0xFF1565C0)
    "image" -> Color(0xFF2E7D32)
    "doc"   -> Color(0xFF1976D2)
    "zip"   -> Color(0xFF6A1B9A)
    else    -> Color(0xFF455A64)
}