package stepup.app222shred.corpapp

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import stepup.app222shred.corpapp.api.ApiService
import stepup.app222shred.corpapp.api.ApiVisitRequest

// FIREBASE
class VisitRequestWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val service: ApiService,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val recordId = inputData.getString("KEY_PUSH_ID")
        val linkUrl = inputData.getString("KEY_LINK_URL")

        val isLinkPresent = !linkUrl.isNullOrEmpty()

        return try {
            val requestBody = ApiVisitRequest(
                recordId = recordId,
                linkUrl = linkUrl
            )

            val response = service.visit(
                pushId = recordId ?: "unknown_id",
                withLink = isLinkPresent,
                request = requestBody
            )

            if (response.isSuccessful) {
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}