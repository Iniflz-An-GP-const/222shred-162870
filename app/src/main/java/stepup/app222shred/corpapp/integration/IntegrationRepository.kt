package stepup.app222shred.corpapp.integration

import android.content.Context
import android.provider.Settings
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class IntegrationRepository(
    private val context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(
        IntegrationState(
            fcmToken = prefs.getString(FCM_TOKEN_KEY, null),
            fcmResolved = prefs.contains(FCM_TOKEN_KEY),
            appsFlyerId = ""
        )
    )
    val state: StateFlow<IntegrationState> = _state.asStateFlow()

    val ready: StateFlow<Boolean> = _state
        .map { it.isReady }
        .stateIn(scope, SharingStarted.Eagerly, _state.value.isReady)

    fun onFcmToken(token: String) {
        prefs.edit().putString(FCM_TOKEN_KEY, token).apply()
        _state.update { it.copy(fcmToken = token, fcmResolved = true) }
    }

    fun payloadJson(): String {
        val s = _state.value
        val map = linkedMapOf<String, Any>(
            "device" to getDeviceId(),
            "fcm_token" to (s.fcmToken ?: ""),
            "appsFlyerId" to ""
        )
        return runCatching { gson.toJson(map) }.getOrDefault("{}")
    }

    private fun getDeviceId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"
    }

    private companion object {
        const val PREFS_NAME = "integration_prefs"
        const val FCM_TOKEN_KEY = "fcm_token"
    }
}