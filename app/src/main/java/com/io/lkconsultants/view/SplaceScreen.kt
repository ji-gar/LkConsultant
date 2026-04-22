package com.io.lkconsultants.view

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.io.lkconsultants.R
import com.room.roomy.retrofit.TokenProvider

import kotlinx.coroutines.delay

@Composable
fun SplaceScreen(onBack:(token:String)->Unit) {

    LaunchedEffect(Unit) {
        delay(2000L)

        if (TokenProvider.getToken().isNotEmpty())
        {
            Log.d("fffds","${TokenProvider.getToken()}")
            onBack.invoke(TokenProvider.getToken())
        }
        else {
            onBack.invoke("")
            Log.d("fffds","${"dddd"}")
        }
    }



    Box(modifier = Modifier.fillMaxSize().background(color = Color.White).windowInsetsPadding(
        WindowInsets.statusBars))
    {
        Column(modifier = Modifier.fillMaxSize().background(color = Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally)
        {

            Image(
                painter = painterResource(R.drawable.lklogo),
                contentScale = ContentScale.Crop,
                contentDescription = "",
                modifier = Modifier.size(150.dp).background(color = Color.White)
            )




        }

    }


}