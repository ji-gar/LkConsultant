package com.room.roomy.retrofit

import android.annotation.SuppressLint
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenProvider.getToken()
        Log.d("Token", token ?: "Token is NULL")

        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request() // send without token
        }

        return chain.proceed(request)
    }
}