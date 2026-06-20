package com.room.roomy.retrofit

import com.io.lkconsultants.model.ConversationResponse
import com.io.lkconsultants.model.CreateConversationRequest
import com.io.lkconsultants.model.CreatedConversation
import com.io.lkconsultants.model.FilesResponse
import com.io.lkconsultants.model.LoginRequest
import com.io.lkconsultants.model.LoginResponse
import com.io.lkconsultants.model.MarkReadRequest
import com.io.lkconsultants.model.MarkReadResponse
import com.io.lkconsultants.model.Message
import com.io.lkconsultants.model.MessageResponse
import com.io.lkconsultants.model.SendMessageResponse
import com.io.lkconsultants.model.UsersListResponse
import com.io.lkconsultants.model.User
import com.io.lkconsultants.model.UserStatus
import com.io.lkconsultants.model.UserStatusListResponse
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

    // Mark all messages in a conversation as read up to now
    @POST("chat/messages/read")
    suspend fun markRead(
        @Body request: MarkReadRequest
    ): Response<MarkReadResponse>

    // Presence: heartbeat marks current user online; called every 60s while in foreground
    @POST("status/heartbeat")
    suspend fun heartbeat(): Response<okhttp3.ResponseBody>

    // Presence: poll online status for all users (or a comma-separated subset)
    @GET("status/users")
    suspend fun getUserStatuses(
        @Query("user_ids") userIds: String? = null
    ): Response<UserStatusListResponse>

    // List all users (for starting a new chat)
    @GET("users")
    suspend fun getUsers(): Response<UsersListResponse>

    // Create (or fetch existing) 1-1 conversation with a user
    @POST("chat/conversations")
    suspend fun createConversation(
        @Body request: CreateConversationRequest
    ): Response<CreatedConversation>

    // ── Employee App Endpoints ──────────────────────────────────────────────

    @GET("leave-requests")
    suspend fun listLeaves(
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<LeaveListResponse>

    @POST("leave-requests")
    suspend fun applyLeave(
        @Body body: ApplyLeaveRequest
    ): Response<ApplyLeaveResponse>

    @GET("tasks")
    suspend fun getTasks(): Response<TaskListResponse>
}

data class Pagination(
    val current_page: Int,
    val per_page: Int,
    val total: Int,
    val last_page: Int
)

data class LeaveListResponse(
    val leave_requests: List<LeaveRequest>,
    val counts: TaskCounts?, // Reusing TaskCounts or similar structure if compatible
    val pagination: Pagination?
)

data class UserBrief(
    val id: Int,
    val name: String,
    val email: String? = null,
    val role: String? = null
)

data class ApplyLeaveRequest(
    val type: String,            // annual | casual | sick | unpaid
    val day_type: String,        // full | half
    val from_date: String,       // YYYY-MM-DD
    val to_date: String? = null,   // required when day_type = full
    val session: String? = null,   // required when day_type = half (first_half | second_half)
    val reason: String
)

data class ApplyLeaveResponse(
    val message: String,
    val leave_request: LeaveRequest?
)

data class LeaveRequest(
    val id: Int,
    val user_id: Int,
    val type: String,
    val from_date: String,
    val to_date: String,
    val day_type: String,
    val session: String?,
    val days: Double,
    val reason: String,
    val status: String,
    val reviewed_by: Int?,
    val reviewed_at: String?,
    val review_note: String?,
    val created_at: String?,
    val updated_at: String?,
    val user: UserBrief?,
    val reviewer: UserBrief?
)

data class ChecklistItem(val id: Int? = null, val text: String, val done: Boolean = false)

data class Task(
    val id: Int,
    val title: String,
    val description: String?,
    val assigned_to: Int,
    val assigned_by: Int,
    val delegated_by: Int?,
    val due_date: String,
    val priority: String,        // low | medium | high
    val status: String,          // pending | in_progress | completed
    val approval_status: String, // not_required | pending | approved | rejected
    val checklist: List<ChecklistItem>?,
    val paths: List<String>?,
    val completed_at: String?,
    val started_at: String?,
    val created_at: String?,
    val updated_at: String?,
    val assignee: UserBrief?,
    val assigner: UserBrief?,
    val delegator: UserBrief?
)

data class TaskListResponse(
    val tasks: List<Task>,
    val counts: TaskCounts? = null
)

data class TaskCounts(
    val pending: Int,
    val in_progress: Int,
    val completed: Int,
    val today: Int,
    val today_open: Int,
    val awaiting_approval: Int
)

