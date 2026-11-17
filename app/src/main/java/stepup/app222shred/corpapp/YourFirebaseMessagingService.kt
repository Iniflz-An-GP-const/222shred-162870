package stepup.app222shred.corpapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import stepup.app222shred.corpapp.api.ApiRequest
import stepup.app222shred.corpapp.api.ApiService
import stepup.app222shred.corpapp.integration.Integrations
import org.koin.android.ext.android.inject
import java.util.Locale
import java.util.TimeZone

class YourFirebaseMessagingService : FirebaseMessagingService() {
    private val serviceScope = CoroutineScope(SupervisorJob())

    private val apiService: ApiService by inject()

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Integrations.repository?.onFcmToken(token)

        val deviceId = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"

        serviceScope.launch {
            try {
                val request = ApiRequest(
                    bundle = packageName,
                    fcm_token = token,
                    device = deviceId,
                    language = Locale.getDefault().toLanguageTag(),
                    timezone = TimeZone.getDefault().id
                )
                apiService.refresh(request)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "New notification"
        val body = remoteMessage.notification?.body ?: ""

        val targetUrl = remoteMessage.data["target_url"]
        val linkUrl = remoteMessage.data["linkUrl"]

        val urlString = when {
            targetUrl != null -> targetUrl
            linkUrl != null -> linkUrl
            else -> null
        }

        val pushId = remoteMessage.data["recordId"] ?: "unknown_id"

        sendNotification(title, body, urlString, pushId)
    }

    private fun sendNotification(title: String, body: String, urlString: String?, pushId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            data = if (!urlString.isNullOrEmpty()) Uri.parse(urlString) else null

            putExtra("KEY_PUSH_ID", pushId)
            putExtra("KEY_LINK_URL", urlString)

            putExtra("ARG_FROM_NOTIFICATION", true)
        }

        val notificationId = ((urlString ?: "") + pushId).hashCode()

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, FCM_DEFAULT_CHANNEL)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FCM_DEFAULT_CHANNEL,
                "Important notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    companion object {
        private const val FCM_DEFAULT_CHANNEL = "fcm_default_channel"
    }
}