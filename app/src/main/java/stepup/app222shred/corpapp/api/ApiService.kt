package stepup.app222shred.corpapp.api

import androidx.annotation.Keep
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("/v1/public/refresh")
    suspend fun refresh(@Body request: ApiRequest): Response<ApiResponse>

    @POST("/v1/public/push/visit")
    suspend fun visit(
        @Query("id") pushId: String,
        @Query("withlink") withLink: Boolean,
        @Body request: ApiVisitRequest
    ): Response<Unit>
}

@Keep
data class ApiRequest(
    val bundle: String,
    val fcm_token: String,
    val device: String,
    val language: String,
    val timezone: String,
    val appsFlyerId: String = "",
)

@Keep
data class ApiResponse(
    val url: String,
    val id: String? = null,
)

@Keep
data class ApiVisitRequest(
    val recordId: String?,
    val linkUrl: String?,
)