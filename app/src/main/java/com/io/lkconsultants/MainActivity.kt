package com.io.lkconsultants

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.io.lkconsultants.color.DarkLKColors
import com.io.lkconsultants.color.LightLKColors
import com.io.lkconsultants.color.LocalLKColors
import com.io.lkconsultants.navscreen.Screens
import com.io.lkconsultants.ui.theme.LkConsultantsTheme
import com.io.lkconsultants.view.ChatScreen
import com.io.lkconsultants.view.LoginScreen
import com.io.lkconsultants.view.NewChatScreen
import com.io.lkconsultants.view.SplaceScreen
import com.io.lkconsultants.view.UsersScreen

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainActivity : FragmentActivity() {

    private var sharedUris by mutableStateOf<List<Uri>>(emptyList())
    private var sharedText by mutableStateOf<String?>(null)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        handleIntent(intent)

        if (com.room.roomy.retrofit.TokenProvider.getToken().isNotEmpty() && 
            com.io.lkconsultants.BuildConfig.FLAVOR == "chatApp") {
            com.io.lkconsultants.reverb.ChatService.start(applicationContext)
        }
        setContent {
            LkConsultantsTheme {
                LKTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        NavStack(sharedUris, sharedText, onSharedHandled = {
                            sharedUris = emptyList()
                            sharedText = null
                        })
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("text/") == true) {
                    sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    sharedUris = emptyList()
                } else {
                    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(Intent.EXTRA_STREAM)
                    }
                    sharedUris = listOfNotNull(uri)
                    sharedText = null
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val uris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
                }
                sharedUris = uris ?: emptyList()
                sharedText = null
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavStack(
    sharedUris: List<Uri>,
    sharedText: String?,
    onSharedHandled: () -> Unit
) {
    var backStack = remember { mutableStateListOf<Screens>(Screens.SplaceScreen) }

    // If we have shared content and we are logged in, make sure we are on the UserScreen
    // so the user can pick who to share with.
    LaunchedEffect(sharedUris, sharedText) {
        if ((sharedUris.isNotEmpty() || sharedText != null) && com.room.roomy.retrofit.TokenProvider.getToken().isNotEmpty()) {
            // If not already on UserScreen, navigate to it (clearing stack if needed or just adding)
            if (backStack.lastOrNull() !is Screens.UserScreen && backStack.lastOrNull() !is Screens.ChatScreen) {
                backStack.add(Screens.UserScreen)
            }
        }
    }

    NavDisplay(
        backStack=backStack,
        onBack = {
            backStack.removeLastOrNull()
        },
        entryProvider = { key ->

           when(key)
           {
               is Screens.LoginScreen -> NavEntry(key){
                val ctx = androidx.compose.ui.platform.LocalContext.current
                LoginScreen(onLoginSuccess = {
                    com.io.lkconsultants.reverb.ChatService.start(ctx.applicationContext)
                    val nextScreen = if (com.io.lkconsultants.BuildConfig.FLAVOR == "chatApp") {
                        Screens.UserScreen
                    } else {
                        Screens.EmployeeHomeScreen
                    }
                    backStack.add(nextScreen)
                })
               }
               is Screens.SplaceScreen -> NavEntry(key){
                   SplaceScreen(){
                       if (it.isEmpty())
                       {
                           backStack.add(Screens.LoginScreen)
                       }
                       else{
                           val nextScreen = if (com.io.lkconsultants.BuildConfig.FLAVOR == "chatApp") {
                               Screens.UserScreen
                           } else {
                               Screens.EmployeeHomeScreen
                           }
                           backStack.add(nextScreen)
                       }
                   }
               }
               is Screens.EmployeeHomeScreen -> NavEntry(key){
                   com.io.lkconsultants.view.EmployeeHomeScreen(
                       onLogout = {
                           com.room.roomy.retrofit.TokenProvider.clear()
                           backStack.clear()
                           backStack.add(Screens.LoginScreen)
                       }
                   )
               }
               is Screens.ChatScreen -> NavEntry(key){
                   val screen = it as Screens.ChatScreen
                   ChatScreen(
                       id = screen.id,
                       participt = screen.participantId,
                       name = screen.name,
                       initialSharedUris = screen.sharedUris?.map { Uri.parse(it) } ?: emptyList(),
                       initialSharedText = screen.sharedText
                   ) {
                       backStack.removeLastOrNull()
                   }
               }
               is Screens.UserScreen -> NavEntry(key){
                   val ctx = androidx.compose.ui.platform.LocalContext.current
                   UsersScreen(
                       onNewChat = { backStack.add(Screens.NewChatScreen) },
                       onLogout = {
                           com.io.lkconsultants.reverb.ChatService.stop(ctx.applicationContext)
                           com.io.lkconsultants.reverb.ReverbManager.disconnect()
                           com.room.roomy.retrofit.TokenProvider.clear()
                           backStack.clear()
                           backStack.add(Screens.LoginScreen)
                       },
                       isShareMode = sharedUris.isNotEmpty() || sharedText != null,
                       onClick = {
                           val myId = com.room.roomy.retrofit.TokenProvider.getUserId().toIntOrNull() ?: -1
                           val other = it.participants.firstOrNull { p -> p.id != myId }
                               ?: it.participants.firstOrNull()
                           val title = it.group_name?.takeIf { n -> n.isNotBlank() }
                               ?: other?.name
                               ?: "Chat"
                           
                           // If we have shared content, pass it to the ChatScreen
                           val sUris = sharedUris.map { it.toString() }
                           val sText = sharedText
                           onSharedHandled() // Mark as handled since we are navigating to the target
                           
                           backStack.add(Screens.ChatScreen(
                               it.id, 
                               participantId = other?.id ?: 0, 
                               name = title,
                               sharedUris = sUris,
                               sharedText = sText
                           ))
                       }
                   )
               }
               is Screens.NewChatScreen -> NavEntry(key){
                   NewChatScreen(
                       onBack = { backStack.removeLastOrNull() },
                       onConversationReady = { convoId, participantId, name ->
                           // If we have shared content, pass it to the ChatScreen
                           val sUris = sharedUris.map { it.toString() }
                           val sText = sharedText
                           onSharedHandled()

                           backStack.removeLastOrNull() // pop NewChatScreen
                           backStack.add(Screens.ChatScreen(
                               convoId, 
                               participantId, 
                               name,
                               sharedUris = sUris,
                               sharedText = sText
                           ))
                       }
                   )
               }
               else -> error("No NavEntry for screen: $key")

           }



        })


}


@Composable
fun LKTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkLKColors else LightLKColors

    CompositionLocalProvider(LocalLKColors provides colors) {
        content()
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LkConsultantsTheme {

    }
}