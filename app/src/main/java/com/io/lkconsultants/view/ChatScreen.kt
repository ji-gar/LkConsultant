package com.io.lkconsultants.view

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.io.lkconsultants.view.LKColors.White
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.io.lkconsultants.color.lkColors
import com.io.lkconsultants.model.ApiSharedFile
import com.io.lkconsultants.viewmodel.ChatViewModel
import com.io.lkconsultants.viewmodel.FilesViewModel
import com.io.lkconsultants.viewmodel.MessagesState
import com.io.lkconsultants.viewmodel.SendMessageState
import com.io.lkconsultants.viewmodel.SendMessageViewModel
import com.room.roomy.retrofit.TokenProvider
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.File
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
fun ChatScreen(
    viewModel: FilesViewModel = viewModel(),
    fileView: ChatViewModel = viewModel(),
    sendMessageViewModel: SendMessageViewModel = viewModel(),
    id: Int,
    participt:Int,
    name: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors  = lkColors                // ← single reference, auto light/dark

    var inputText  by remember { mutableStateOf("") }
    var downloadId by remember { mutableStateOf<Long?>(null) }
    var isSending  by remember { mutableStateOf(false) }

    val state1           by fileView.state.collectAsStateWithLifecycle()
    val sendMessageState  = sendMessageViewModel.state.collectAsStateWithLifecycle().value
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(id) {
        fileView.connect(TokenProvider.getToken(), id.toString())
    }
    LaunchedEffect(sendMessageState) {
        if (sendMessageState is SendMessageState.Success) {
            inputText = ""
            selectedUri=null

            fileView.getMessages(id)
            isSending = false
            sendMessageViewModel.resetState()
        }
    }
    LaunchedEffect(Unit) { fileView.getMessages(id) }
   // var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }


//    val launcher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetMultipleContents()
//    ) { uris: List<Uri> ->
//        selectedUris = uris
//    }

        val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uris: Uri? ->
        selectedUri = uris
    }



    val messages = (state1 as? MessagesState.Success)?.messages ?: emptyList()
    val isLoading = state1 is MessagesState.Loading
    val errorMsg  = (state1 as? MessagesState.Error)?.message

    Log.d("Messageid",messages.toString())

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp, color = colors.surface) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                colors.accentBlue,
                                                colors.primaryBlue
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    name.take(2).uppercase(),
                                    color = colors.white, fontSize = 14.sp, fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
                                Text(
                                    when (state1) {
                                        is MessagesState.Loading -> "Loading…"
                                        is MessagesState.Success -> "Super Admin • ${messages.size} messages"
                                        is MessagesState.Error   -> "Error loading"
                                        else                     -> ""
                                    },
                                    fontSize = 11.sp, color = colors.subtitle
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.primaryBlue)
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = colors.primaryBlue)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = colors.primaryBlue)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
                )
            }
        },

        bottomBar = {
            Surface(shadowElevation = 8.dp, color = colors.surface) {
                Column {


                  if (selectedUri!=null)
                  {
                      FileItem(selectedUri!!){
                          selectedUri=null
                      }
                  }
//                    LazyRow() {
//                        items(selectedUris) { uri ->
//                            FileItem(uri)
//                        }
//                    }


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            launcher.launch("*/*")

                        }) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Attach",
                                tint = colors.primaryBlue
                            )
                        }
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    "Type a message…",
                                    color = colors.subtitle,
                                    fontSize = 14.sp
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primaryBlue,
                                unfocusedBorderColor = colors.divider,
                                focusedTextColor = colors.onSurface,
                                unfocusedTextColor = colors.onSurface,
                                cursorColor = colors.primaryBlue
                            ),
                            singleLine = true
                        )
                        Spacer(Modifier.width(8.dp))

                        isSending = sendMessageState is SendMessageState.Loading

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(colors.primaryBlue)
                                .clickable(enabled = !isSending && (inputText.isNotBlank() || selectedUri != null)) {
                                   if (selectedUri!=null)
                                   {
                                       Log.d("dddd","cddd")
                                       sendMessageViewModel.sendMessage(id, "${getFileName(context,selectedUri!!)}",uriToFile(context,selectedUri!!))
                                   }
                                    else {
                                       Log.d("dddd","notfile")
                                       sendMessageViewModel.sendMessage(id, inputText)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        )
                        {
                            if (isSending) {
                                CircularProgressIndicator(
                                    color = colors.white,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = colors.white,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                }
            }
        },

        containerColor = colors.background

    ) { padding ->

        if (isLoading) {
            Box(Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primaryBlue)
            }
            return@Scaffold
        }

        if (errorMsg != null) {
            Box(Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(errorMsg, color = colors.brandRed)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.getFiles() },
                        colors  = ButtonDefaults.buttonColors(containerColor = colors.primaryBlue)
                    ) { Text("Retry", color = colors.white) }
                }
            }
            return@Scaffold
        }

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
                    Surface(shape = RoundedCornerShape(12.dp), color = colors.divider) {
                        Text(
                            "Today",
                            modifier   = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            fontSize   = 11.sp,
                            color      = colors.subtitle,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
            val currentUserId = TokenProvider.getUserId().toInt()

            items(messages) { message ->

                val isMine = message.sender?.id == currentUserId

                Log.d(
                    "MineCheck",
                    "isMine=$isMine sender=${message.sender?.id} sender_id=${message.sender_id} current=$currentUserId"
                )
         Log.d("Mine","${isMine}${message.sender_id}  ${currentUserId}")
                if (message.file_name != null) {
                    FileBubble(
                         ChatMessage.FileMsg(
                            file = SharedFile(
                                id        = 1,
                                file_name = message.file_name,
                                file_url  = message.file_url,
                                size      = "10",
                                sender    = message.sender.name,
                                type      = getFileExtension(message.file_name).orEmpty(),
                                time      = formatTime(message.created_at),
                                message   = ""
                            ),
                            isMine = isMine,
                            time   = formatTime(message.created_at)
                        ),
                        onDownload = {
                            downloadId = donwload(context, message.file_url.toString(), message.file_name)
                        },
                        onShare = {
                            val did = downloadId
                            if (did != null) {
                                val uri = getDownloadedFileUri(context, did)
                                if (uri != null) shareFile(context, uri)
                                else Toast.makeText(context, "File not downloaded yet", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Please download first", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                } else {
                    TextBubble(
                        ChatMessage.TextMsg(
                            text   = message.text.orEmpty(),
                            isMine = isMine,
                            time   = formatTime(message.created_at)
                        )
                    )
                }
            }
        }
    }
}

fun getFileExtension(fileName: String?): String? =
    fileName?.substringAfterLast('.', "")?.takeIf { it.isNotEmpty() }


fun uriToFile(context: Context, uri: Uri): File {
    val contentResolver = context.contentResolver
    val fileName = getFileName(context, uri)
    Log.d("ffsd",fileName.toString())

    val tempFile = File(context.cacheDir, fileName)

    contentResolver.openInputStream(uri)?.use { inputStream ->
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }

    return tempFile
}




@Composable
fun FileItem(uri: Uri,onClick:()-> Unit) {
    val context = LocalContext.current
    val mimeType = context.contentResolver.getType(uri)

    val isImage = mimeType?.startsWith("image") == true

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (isImage) {
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = getFileName(context, uri),
            maxLines = 2,
           modifier =  Modifier.weight(1f)
        )
        Icon(
            modifier = Modifier.clickable{
             onClick.invoke()
            },
            imageVector = Icons.Default.Clear,
            contentDescription = "",
            tint =  if (isSystemInDarkTheme()) Color.White else Color.Black
        )


    }
}

fun getFileName(context: Context, uri: Uri): String {
    var name = "Unknown"

    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst()) {
            name = it.getString(index)
        }
    }
    return name
}
fun getUniqueFileName(context: Context, uri: Uri): String {
    var originalName = "file"

    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && index != -1) {
            originalName = it.getString(index)
        }
    }

    // Get extension
    val extension = originalName.substringAfterLast('.', "")

    // Simple unique name (timestamp only)
    val uniqueName = System.currentTimeMillis().toString()

    return if (extension.isNotEmpty()) {
        "$uniqueName.$extension"
    } else {
        uniqueName
    }
}





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
        horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start
    ) {
        var lkcolor=lkColors

        // Show avatar only for OTHER user (left side)
        if (!message.isMine) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(lkcolor.primaryBlue)
            )
            Spacer(Modifier.width(6.dp))
        }

        Column(
            horizontalAlignment = if (message.isMine) Alignment.End else Alignment.Start
        ) {

            Surface(
                shape = RoundedCornerShape(
                    topStart = if (message.isMine) 18.dp else 4.dp,
                    topEnd   = if (message.isMine) 4.dp else 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 18.dp
                ),
                color = if (message.isMine) lkcolor.chatBubbleUser else lkcolor.chatBubbleAdmin,
                shadowElevation = 1.dp
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = if (message.isMine) lkcolor.subtitle else lkcolor.white,
                    fontSize = 14.sp
                )
            }

            Text(
                message.time,
                fontSize = 10.sp,
                color = lkcolor.subtitle,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
//  FILE BUBBLE
// ─────────────────────────────────────────────
@Composable
fun FileBubble(message: ChatMessage.FileMsg, onDownload:()->Unit,onShare: () -> Unit) {
    val file = message.file
    val lkcolor=lkColors
    Row(Modifier.fillMaxWidth(), horizontalArrangement  = if (message.isMine) Arrangement.End else Arrangement.Start) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(lkcolor.primaryBlue),
            contentAlignment = Alignment.Center
        ) {  }
        Spacer(Modifier.width(6.dp))

        Column {
            Text("Super Admin", fontSize = 10.sp, color = lkcolor.primaryBlue,
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
                                color    = lkcolor.white,
                                fontSize = 10.sp,
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(file.file_name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                color = lkcolor.primaryBlue, maxLines = 1,
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
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp),
                            shape    = RoundedCornerShape(8.dp),
                            border   = BorderStroke(1.dp, lkcolor.primaryBlue),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, null,
                                tint = lkcolor.primaryBlue, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Download", color = LKColors.PrimaryBlue,
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        // Share
                        Button(
                            onClick  = onShare,
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp),
                            shape    = RoundedCornerShape(8.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = LKColors.PrimaryBlue),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Share, null,
                                tint = lkcolor.white, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Share", color = lkcolor.white,
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Text(message.time, fontSize = 10.sp, color = lkcolor.subtitle,
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