package stepup.app222shred.corpapp.integration

import androidx.annotation.Keep

@Keep
data class IntegrationState(
    val fcmToken: String? = null,
    val fcmResolved: Boolean = false,
    val appsFlyerId: String = ""
) {
    val isReady: Boolean get() = fcmResolved
}