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
import com.io.lkconsultants.model.UserStatusListResponse
import com.google.gson.Gson
import android.util.Log
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

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

interface getApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("files")
    suspend fun getFiles(
        @Query("type") type: String? = null,
        @Query("user_id") userId: Int? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<FilesResponse>

    @GET("chat/conversations")
    suspend fun getConversations(): Response<List<ConversationResponse>>

    @GET("chat/messages")
    suspend fun getMessages(@Query("conversationId") conversationId: Int): Response<Message>

    @Multipart
    @POST("chat/messages")
    suspend fun sendMessage(
        @Part("conversationId") conversationId: RequestBody,
        @Part("text") text: RequestBody,
        @Part file: MultipartBody.Part? = null
    ): Response<SendMessageResponse>

    @POST("chat/messages/read")
    suspend fun markRead(@Body request: MarkReadRequest): Response<MarkReadResponse>

    @POST("status/heartbeat")
    suspend fun heartbeat(): Response<ResponseBody>

    @GET("status/users")
    suspend fun getUserStatuses(@Query("user_ids") userIds: String? = null): Response<UserStatusListResponse>

    @GET("users")
    suspend fun getUsers(): Response<UsersListResponse>

    @POST("chat/conversations")
    suspend fun createConversation(@Body request: CreateConversationRequest): Response<CreatedConversation>

    // ── Employee App Endpoints (EMS Guide) ──────────────────────────────────────────

    // ── Leave ──────────────────────────────────────────
    @GET("leave-requests")
    suspend fun listLeaves(
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<LeaveListResponse>

    @POST("leave-requests")
    suspend fun applyLeave(@Body body: ApplyLeaveRequest): Response<ApplyLeaveResponse>

    @PUT("leave-requests/{id}/review")
    suspend fun reviewLeave(@Path("id") id: Int, @Body body: ReviewLeaveRequest): Response<ApplyLeaveResponse>

    @GET("leave-requests/upcoming")
    suspend fun upcomingLeaves(): Response<UpcomingLeavesResponse>

    @GET("leave-requests/report")
    suspend fun leaveReport(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<LeaveReportResponse>

    @DELETE("leave-requests/{id}")
    suspend fun deleteLeave(@Path("id") id: Int): Response<MessageResponse>

    @GET("leave-policy")
    suspend fun getLeavePolicy(): Response<LeavePolicyResponse>

    @GET("holidays")
    suspend fun getHolidays(@Query("year") year: Int? = null): Response<HolidayResponse>

    // ── Task ───────────────────────────────────────────
    @GET("tasks")
    suspend fun listTasks(
        @Query("today") today: Int? = null,
        @Query("status") status: String? = null,
        @Query("approval_status") approvalStatus: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<TaskListResponse>

    @POST("tasks")
    suspend fun createTask(@Body body: CreateTaskRequest): Response<TaskResponse>

    @POST("tasks/{id}/approve")
    suspend fun approveTask(@Path("id") id: Int): Response<TaskResponse>

    @POST("tasks/{id}/reject")
    suspend fun rejectTask(@Path("id") id: Int): Response<TaskResponse>

    @POST("tasks/{id}/delegate")
    suspend fun delegateTask(@Path("id") id: Int, @Body body: AssignToRequest): Response<TaskResponse>

    @PUT("tasks/{id}/status")
    suspend fun updateTaskStatus(@Path("id") id: Int, @Body body: UpdateStatusRequest): Response<TaskResponse>

    @PUT("tasks/{id}/complete")
    suspend fun completeTask(@Path("id") id: Int): Response<TaskResponse>

    @PUT("tasks/{id}/checklist")
    suspend fun toggleChecklist(@Path("id") id: Int, @Body body: ToggleChecklistRequest): Response<TaskResponse>

    @POST("tasks/{id}/reassign")
    suspend fun reassignTask(@Path("id") id: Int, @Body body: AssignToRequest): Response<TaskResponse>

    @POST("tasks/{id}/complete-self")
    suspend fun completeSelf(@Path("id") id: Int): Response<TaskResponse>

    @DELETE("tasks/{id}/revoke")
    suspend fun revokeTask(@Path("id") id: Int): Response<MessageResponse>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Int): Response<MessageResponse>

    @GET("tasks/{id}/activities")
    suspend fun taskActivities(@Path("id") id: Int): Response<TaskActivitiesResponse>

    @Streaming
    @GET("tasks/export")
    suspend fun exportTasks(
        @Query("status") status: String? = null,
        @Query("search") search: String? = null
    ): Response<ResponseBody>
    
    // Legacy mapping
    @GET("tasks")
    suspend fun getTasks(): Response<TaskListResponse>
}

data class UserBrief(
    val id: Int,
    val name: String,
    val email: String? = null,
    val role: String? = null
)

// ── Leave ──────────────────────────────────────────────
data class LeaveRequest(
    val id: Int,
    val user_id: Int,
    val type: String,            // annual | casual | sick | unpaid
    val from_date: String,
    val to_date: String,
    val day_type: String,        // full | half
    val session: String?,        // first_half | second_half | null
    val days: Double,
    val reason: String,
    val status: String,          // pending | approved | rejected
    val reviewed_by: Int?,
    val reviewed_at: String?,
    val review_note: String?,
    val created_at: String?,
    val updated_at: String?,
    val user: UserBrief?,
    val reviewer: UserBrief?
)

data class LeaveListResponse(
    val leave_requests: List<LeaveRequest>,
    val counts: LeaveCounts,
    val pagination: Pagination
)

data class LeaveCounts(val pending: Int, val approved: Int, val rejected: Int)

data class LeavePolicyResponse(
    val about: String?,
    val policies: List<LeavePolicy>
)

data class LeavePolicy(
    val id: Int,
    val name: String,
    val count: Double,
    val min_notice_days: Int,
    val description: String?,
    val used: Double,
    val remaining: Double
)

data class HolidayResponse(
    val holidays: List<Holiday>
)

data class Holiday(
    val id: Int,
    val name: String,
    val date: String,
    val description: String?
)

data class ApplyLeaveRequest(
    val type: String,
    val day_type: String,
    val from_date: String,
    val to_date: String? = null,   // required when day_type = full
    val session: String? = null,   // required when day_type = half
    val reason: String
)

data class ReviewLeaveRequest(val status: String, val review_note: String? = null)

data class LeaveReportResponse(
    val from: String,
    val to: String,
    val report: List<LeaveReportRow>
)

data class LeaveReportRow(
    val user: UserBrief,
    val annual: Double,
    val casual: Double,
    val sick: Double,
    val unpaid: Double,
    val total: Double
)

data class ApplyLeaveResponse(val message: String, val leave_request: LeaveRequest)
data class UpcomingLeavesResponse(val leave_requests: List<LeaveRequest>, val window_days: Int)

// ── Task ───────────────────────────────────────────────
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
    val counts: TaskCounts,
    val pagination: Pagination? = null  // null when today=1
)

data class TaskCounts(
    val pending: Int,
    val in_progress: Int,
    val completed: Int,
    val today: Int,
    val today_open: Int,
    val awaiting_approval: Int
)

data class CreateTaskRequest(
    val title: String,
    val description: String? = null,
    val assigned_to: Int,
    val due_date: String,
    val priority: String? = null,
    val checklist: List<ChecklistItem>? = null,
    val paths: List<String>? = null
)

data class AssignToRequest(val assigned_to: Int)         // delegate / reassign
data class UpdateStatusRequest(val status: String)        // pending | in_progress
data class ToggleChecklistRequest(val item_id: Int, val done: Boolean)

data class TaskResponse(val message: String, val task: Task)

data class Pagination(
    val current_page: Int,
    val per_page: Int,
    val total: Int,
    val last_page: Int
)

data class TaskActivitiesResponse(val activities: List<TaskActivity>)

data class TaskActivity(
    val id: Int,
    val task_id: Int,
    val actor_id: Int?,
    val action: String,
    val from_user_id: Int?,
    val to_user_id: Int?,
    val note: String?,
    val created_at: String?,
    val updated_at: String?,
    val actor: UserBrief?,
    val from_user: UserBrief?,
    val to_user: UserBrief?
)

data class ApiError(
    val message: String?,
    val error: String?,
    val errors: Map<String, List<String>>?
) {
    fun text(fallback: String) =
        errors?.values?.firstOrNull()?.firstOrNull() ?: message ?: error ?: fallback
}

fun <T> Response<T>.getErrorText(fallback: String = "Something went wrong"): String {
    return try {
        val errorBody = errorBody()?.string()
        Log.d("RetrofitInstance", "Error Body: $errorBody")
        if (errorBody != null) {
            val apiError = Gson().fromJson(errorBody, ApiError::class.java)
            val msg = apiError.text(fallback)
            Log.d("RetrofitInstance", "Parsed Message: $msg")
            msg
        } else {
            fallback
        }
    } catch (e: Exception) {
        Log.e("RetrofitInstance", "Error parsing error body", e)
        fallback
    }
}
