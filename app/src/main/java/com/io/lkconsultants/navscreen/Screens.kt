package com.io.lkconsultants.navscreen

import androidx.navigation3.runtime.NavKey
import com.io.lkconsultants.model.ConversationResponse
import com.io.lkconsultants.model.Participant
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screens : NavKey {

    @Serializable
    object SplaceScreen : Screens

    @Serializable
    object LoginScreen: Screens

    @Serializable
    data class ChatScreen(
        var id: Int,
        var participantId: Int,
        var name: String,
        var sharedUris: List<String>? = null,
        var sharedText: String? = null
    ) : Screens

    @Serializable
    object UserScreen : Screens

    @Serializable
    object EmployeeHomeScreen : Screens

    @Serializable
    object NewChatScreen : Screens
}