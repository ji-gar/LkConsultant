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
import com.io.lkconsultants.view.SplaceScreen
import com.io.lkconsultants.view.UsersScreen

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LkConsultantsTheme {


                LKTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        NavStack()
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavStack() {


    var backStack = remember { mutableStateListOf<Screens>(Screens.SplaceScreen) }


    NavDisplay(

        backStack=backStack,
        onBack = {
            backStack.removeLastOrNull()
        },
        entryProvider = { key ->

           when(key)
           {
               is Screens.LoginScreen -> NavEntry(key){
                LoginScreen(onLoginSuccess = {
                    backStack.add(Screens.UserScreen)
                })
               }
               is Screens.SplaceScreen -> NavEntry(key){
                   SplaceScreen(){
                       if (it.isEmpty())
                       {
                           backStack.add(Screens.LoginScreen)
                       }
                       else{
                           backStack.add(Screens.UserScreen)
                       }
                   }
               }
               is Screens.ChatScreen -> NavEntry(key){
                   var screen=it as Screens.ChatScreen
                   ChatScreen(id = screen.id,
                       participt=screen.participantId,
                       name = screen.name){
                       backStack.removeLastOrNull()
                   }
               }
               is Screens.UserScreen -> NavEntry(key){


                   UsersScreen(){

                       backStack.add(Screens.ChatScreen(it.id, participantId = it.participants.get(0).id ,name = it.participants.get(0).name))

                   }
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