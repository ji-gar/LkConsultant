package com.room.roomy.retrofit

import com.io.lkconsultants.model.ConversationResponse
import com.io.lkconsultants.model.FilesResponse
import com.io.lkconsultants.model.LoginRequest
import com.io.lkconsultants.model.LoginResponse
import com.io.lkconsultants.model.Message
import com.io.lkconsultants.model.MessageResponse
import com.io.lkconsultants.model.SendMessageResponse
import com.io.lkconsultants.model.User
import com.io.lkconsultants.model.UserStatus
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url

object RetrofitInstance {

    val retrofits : getApiService get() {

        val okhttpBody= HttpLoggingInterceptor().apply {
            level= HttpLoggingInterceptor.Level.BODY
        }


         val okHttpClient= OkHttpClient.Builder()
             .addInterceptor(AuthInterceptor())
             .addInterceptor(okhttpBody)
             .build()


        val retrofit = Retrofit.Builder()
            .baseUrl(URL.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return  retrofit.create<getApiService>(getApiService::class.java)

    }
}
interface  getApiService  {
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("files")
    suspend fun getFiles(
        @Query("type")     type: String? = null,   // "sent" | "received" | "all" | null = all
        @Query("user_id")  userId: Int?  = null,
        @Query("page")     page: Int     = 1,
        @Query("per_page") perPage: Int  = 20
    ): Response<FilesResponse>

    // Users
    @GET("chat/conversations")
    suspend fun getConversations(): Response<List<ConversationResponse>>

    @GET("chat/messages")
    suspend fun getMessages(
        @Query("conversationId") conversationId: Int
    ): Response<Message>


    @Multipart
    @POST("chat/messages")
    suspend fun sendMessage(
        @Part("conversationId") conversationId: RequestBody,
        @Part("text")           text: RequestBody,
        @Part               file: MultipartBody.Part? = null
    ): Response<SendMessageResponse>




}

