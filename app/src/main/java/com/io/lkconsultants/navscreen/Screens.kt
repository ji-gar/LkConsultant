package com.io.lkconsultants.navscreen

import androidx.navigation3.runtime.NavKey
import com.io.lkconsultants.model.ConversationResponse
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screens : NavKey {

    @Serializable
    object SplaceScreen : Screens

    @Serializable
    object LoginScreen: Screens

    @Serializable
    data class ChatScreen(var id: Int,var name:String) : Screens

    @Serializable
    object UserScreen : Screens


}