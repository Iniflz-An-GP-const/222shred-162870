package stepup.app222shred.corpapp.ui

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.MediaStore
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

private const val CLOSE_BRIDGE_NAME = "AndroidCloseBridge"

private val BLOCKED_DOWNLOAD_MIME_TYPES = setOf(
    "application/vnd.android.package-archive",
    "application/octet-stream",
)

private val IN_WEBVIEW_SCHEMES = setOf("http", "https", "about", "data", "file")

private const val GATE_BRIDGE_NAME = "AndroidGateBridge"
private const val INTEGRATIONS_BRIDGE_NAME = "AndroidIntegrations"
const val GATE_BOOTSTRAP_URL = "file:///android_asset/index.html"

/** Отдаёт JS JSON с данными интеграций для тела запроса. */
private class IntegrationsBridge(private val provider: () -> String) {
    @JavascriptInterface
    fun payload(): String = provider()
}

private class CloseBridge(private val onClose: () -> Unit) {
    @JavascriptInterface
    fun closeWindow() {
        Handler(Looper.getMainLooper()).post { onClose() }
    }
}

private class GateBridge(
    private val linkFoundCallback: (String) -> Unit,
    private val contentLoadedCallback: () -> Unit,
) {
    @JavascriptInterface
    fun onLinkFound(url: String) {
        Handler(Looper.getMainLooper()).post { linkFoundCallback(url) }
    }

    @JavascriptInterface
    fun contentLoaded() {
        Handler(Looper.getMainLooper()).post { contentLoadedCallback() }
    }
}

private fun injectCloseOverride(view: WebView?) {
    view?.evaluateJavascript(
        """
        (function() {
            window.close = function() {
                if (window.$CLOSE_BRIDGE_NAME) { window.$CLOSE_BRIDGE_NAME.closeWindow(); }
            };
        })();
        """.trimIndent(),
        null,
    )
}

private fun isBlockedDownload(url: String, mimeType: String?): Boolean {
    val extension = url.substringBefore('?').substringAfterLast('.', "").lowercase()
    return mimeType.orEmpty() in BLOCKED_DOWNLOAD_MIME_TYPES || extension == "apk"
}

private fun openExternally(context: Context, uri: Uri): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        true
    }
}

private fun resourceToPermission(resource: String): String? = when (resource) {
    PermissionRequest.RESOURCE_VIDEO_CAPTURE -> Manifest.permission.CAMERA
    PermissionRequest.RESOURCE_AUDIO_CAPTURE -> Manifest.permission.RECORD_AUDIO
    else -> null
}

private fun hasPermission(context: Context, permission: String): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED

private fun createCapturePhotoFile(context: Context): File {
    val dir = File(context.cacheDir, "captures").apply { mkdirs() }
    return File.createTempFile("capture_", ".jpg", dir)
}

private fun configureWebView(
    webView: WebView,
    context: Context,
    agent: String,
    onOpenPopup: (WebView) -> Boolean,
    onPermissionRequest: (PermissionRequest) -> Unit,
    onShowFileChooser: (WebView, ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Boolean,
) {
    webView.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT,
    )

    webView.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        cacheMode = WebSettings.LOAD_DEFAULT
        userAgentString = agent
        javaScriptCanOpenWindowsAutomatically = true
        setSupportMultipleWindows(true)
        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        mediaPlaybackRequiresUserGesture = false
    }

    val cookieManager = CookieManager.getInstance()
    cookieManager.setAcceptCookie(true)
    cookieManager.setAcceptThirdPartyCookies(webView, true)

    webView.setDownloadListener { url, _, _, mimeType, _ ->
        // APK/бинарные загрузки игнорируем; остальное не обрабатываем.
        isBlockedDownload(url, mimeType)
    }

    webView.webChromeClient = object : WebChromeClient() {
        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            // Подавляем проброс console.* в Logcat.
            return true
        }

        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?,
        ): Boolean {
            val popup = WebView(context)
            if (!onOpenPopup(popup)) {
                popup.destroy()
                return false
            }

            val transport = resultMsg?.obj as? WebView.WebViewTransport
            transport?.webView = popup
            resultMsg?.sendToTarget()
            return true
        }

        override fun onPermissionRequest(request: PermissionRequest?) {
            request ?: return
            onPermissionRequest(request)
        }

        override fun onShowFileChooser(
            view: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?,
        ): Boolean {
            if (view == null || filePathCallback == null || fileChooserParams == null) return false
            return onShowFileChooser(view, filePathCallback, fileChooserParams)
        }
    }
}

@Composable
private fun WebViewPopupDialog(
    webView: WebView,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BackHandler {
            if (webView.canGoBack()) webView.goBack() else onDismiss()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .systemBarsPadding(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = "✕",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onDismiss() },
                )
            }
            AndroidView(
                factory = { webView },
                modifier = Modifier.fillMaxSize(),
            )
        }

        DisposableEffect(webView) {
            onDispose { webView.destroy() }
        }
    }
}

@Composable
fun DynamicScreen(
    url: String,
    agent: String,
    isConnected: Boolean,
    onPageLoaded: (() -> Unit),
    onCloseRequested: () -> Unit = {},
    onNoOfferFound: () -> Unit = {},
    onFinalUrlResolved: (String) -> Unit = {},
    integrationsProvider: () -> String = { "{}" },
) {
    val context = LocalContext.current
    val currentIntegrationsProvider by rememberUpdatedState(integrationsProvider)
    val currentOnPageLoaded by rememberUpdatedState(onPageLoaded)
    val currentOnCloseRequested by rememberUpdatedState(onCloseRequested)
    val currentOnNoOfferFound by rememberUpdatedState(onNoOfferFound)
    val currentOnFinalUrlResolved by rememberUpdatedState(onFinalUrlResolved)

    var isFirstPageLoaded by rememberSaveable { mutableStateOf(false) }
    val webViewStateBundle = rememberSaveable { Bundle() }

    var isLoading by rememberSaveable { mutableStateOf(true) }
    var canGoBack by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    var popupWebView by remember { mutableStateOf<WebView?>(null) }

    var wasCatchDetected by remember { mutableStateOf(false) }
    var hasHandledGateLink by remember { mutableStateOf(false) }
    var hasSavedFinalUrl by remember { mutableStateOf(false) }
    var didTriggerNoOfferFound by remember { mutableStateOf(false) }
    var gateHost by remember { mutableStateOf<String?>(null) }

    fun triggerNoOfferFoundOnce() {
        if (didTriggerNoOfferFound) return
        didTriggerNoOfferFound = true
        currentOnNoOfferFound()
    }

    var pendingPermissionRequest by remember { mutableStateOf<PermissionRequest?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        val req = pendingPermissionRequest
        pendingPermissionRequest = null
        if (req == null) return@rememberLauncherForActivityResult
        val granted = req.resources.filter {
            resourceToPermission(it)?.let { p ->
                hasPermission(context, p)
            } != false
        }
        if (granted.size == req.resources.size) req.grant(req.resources) else req.deny()
    }
    val onPermissionRequest: (PermissionRequest) -> Unit = { request ->
        val needed = request.resources.mapNotNull { resourceToPermission(it) }
            .filter { !hasPermission(context, it) }
        if (needed.isEmpty()) {
            request.grant(request.resources)
        } else {
            pendingPermissionRequest = request
            permissionLauncher.launch(needed.toTypedArray())
        }
    }

    var pendingFileCallback by remember {
        mutableStateOf<ValueCallback<Array<Uri>>?>(null)
    }
    var pendingCapturedUri by remember { mutableStateOf<Uri?>(null) }
    val fileChooserLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val callback = pendingFileCallback
        pendingFileCallback = null
        val captured = pendingCapturedUri
        pendingCapturedUri = null
        if (callback == null) return@rememberLauncherForActivityResult

        val resultUris =
            WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        val uris = when {
            !resultUris.isNullOrEmpty() -> resultUris
            result.resultCode == Activity.RESULT_OK && captured != null -> arrayOf(captured)
            else -> null
        }
        callback.onReceiveValue(uris)
    }
    val onShowFileChooser: (WebView, ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Boolean =
        { _, filePathCallback, fileChooserParams ->
            pendingFileCallback = filePathCallback

            val contentIntent = fileChooserParams.createIntent()

            val captureIntent = if (hasPermission(context, Manifest.permission.CAMERA)) {
                try {
                    val photoFile = createCapturePhotoFile(context)
                    val photoUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        photoFile
                    )
                    pendingCapturedUri = photoUri
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                        addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    }
                } catch (e: Exception) {
                    null
                }
            } else null

            val chooser = Intent.createChooser(contentIntent, "Choose file").apply {
                if (captureIntent != null) putExtra(
                    Intent.EXTRA_INITIAL_INTENTS,
                    arrayOf(captureIntent)
                )
            }

            try {
                fileChooserLauncher.launch(chooser)
                true
            } catch (e: ActivityNotFoundException) {
                pendingFileCallback = null
                pendingCapturedUri = null
                false
            }
        }

    BackHandler(enabled = canGoBack) {
        webView?.goBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    configureWebView(
                        webView = this,
                        context = ctx,
                        agent = agent,
                        onOpenPopup = { popup ->
                            configureWebView(
                                webView = popup,
                                context = ctx,
                                agent = agent,
                                onOpenPopup = { false },
                                onPermissionRequest = onPermissionRequest,
                                onShowFileChooser = onShowFileChooser,
                            )
                            popup.addJavascriptInterface(
                                CloseBridge { popupWebView = null },
                                CLOSE_BRIDGE_NAME,
                            )
                            popup.webViewClient = object : WebViewClient() {
                                override fun onPageStarted(
                                    view: WebView?,
                                    pageUrl: String?,
                                    favicon: Bitmap?
                                ) {
                                    injectCloseOverride(view)
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                ): Boolean {
                                    val req = request ?: return false
                                    if (!req.isForMainFrame) return false
                                    val uri = req.url
                                    if (uri.scheme?.lowercase()
                                            .orEmpty() in IN_WEBVIEW_SCHEMES
                                    ) return false
                                    return openExternally(ctx, uri)
                                }
                            }
                            popupWebView = popup
                            true
                        },
                        onPermissionRequest = onPermissionRequest,
                        onShowFileChooser = onShowFileChooser,
                    )

                    addJavascriptInterface(
                        CloseBridge { currentOnCloseRequested() },
                        CLOSE_BRIDGE_NAME,
                    )

                    addJavascriptInterface(
                        IntegrationsBridge { currentIntegrationsProvider() },
                        INTEGRATIONS_BRIDGE_NAME,
                    )

                    addJavascriptInterface(
                        GateBridge(
                            linkFoundCallback = { link ->
                                if (!hasHandledGateLink) {
                                    hasHandledGateLink = true
                                    if (link.startsWith("http://", true) ||
                                        link.startsWith("https://", true)
                                    ) {
                                        gateHost = Uri.parse(link).host
                                        // Кэшируем ссылку оффера от сервера: при следующих запусках
                                        // грузим её напрямую, без index.html и повторного запроса.
                                        if (!hasSavedFinalUrl) {
                                            hasSavedFinalUrl = true
                                            currentOnFinalUrlResolved(link)
                                        }
                                        loadUrl(link)
                                    } else {
                                        triggerNoOfferFoundOnce()
                                    }
                                }
                            },
                            contentLoadedCallback = { triggerNoOfferFoundOnce() },
                        ),
                        GATE_BRIDGE_NAME,
                    )

                    webViewClient = object : WebViewClient() {

                        override fun onPageStarted(
                            view: WebView?,
                            pageUrl: String?,
                            favicon: Bitmap?
                        ) {
                            injectCloseOverride(view)
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?,
                        ): Boolean {
                            val req = request ?: return false
                            if (!req.isForMainFrame) return false
                            val uri = req.url
                            val urlString = uri.toString()

                            if (urlString.contains("catch.php")) {
                                wasCatchDetected = true
                                return false
                            }

                            if (wasCatchDetected && gateHost != null &&
                                uri.host.equals(gateHost, ignoreCase = true)
                            ) {
                                wasCatchDetected = false
                                triggerNoOfferFoundOnce()
                                return true
                            }

                            if (uri.scheme?.lowercase()
                                    .orEmpty() in IN_WEBVIEW_SCHEMES
                            ) return false
                            return openExternally(ctx, uri)
                        }

                        override fun onPageFinished(view: WebView?, loadedUrl: String?) {
                            canGoBack = view?.canGoBack() == true
                            isLoading = false
                            view?.saveState(webViewStateBundle)

                            CookieManager.getInstance().flush()

                            if (!isFirstPageLoaded) {
                                isFirstPageLoaded = true
                                currentOnPageLoaded()
                            }

                            if (!hasSavedFinalUrl && loadedUrl != null &&
                                loadedUrl != GATE_BOOTSTRAP_URL &&
                                !loadedUrl.contains("catch.php") &&
                                !Uri.parse(loadedUrl).host.equals(gateHost, ignoreCase = true)
                            ) {
                                hasSavedFinalUrl = true
                                currentOnFinalUrlResolved(loadedUrl)
                            }
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?,
                        ) {
                            if (request?.isForMainFrame == true && isConnected) {
                                triggerNoOfferFoundOnce()
                            }
                            if (!isConnected) {
                                view?.stopLoading()
                            }
                        }

                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: SslErrorHandler?,
                            error: SslError?,
                        ) {
                            handler?.cancel()
                        }
                    }

                    if (webViewStateBundle.isEmpty) {
                        loadUrl(url, mapOf("X-Requested-With" to ""))
                    } else {
                        restoreState(webViewStateBundle)
                        if (!isFirstPageLoaded) {
                            isFirstPageLoaded = true
                            isLoading = false
                            currentOnPageLoaded()
                        }
                    }

                    webView = this
                }
            },

            update = { view ->
                if (!isConnected) {
                    view.stopLoading()
                }
            },

            onRelease = { view ->
                view.saveState(webViewStateBundle)
                webView = null
            },

            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            CircularProgressIndicator()
        }

        if (!isConnected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No Internet Connection", color = Color.White, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Please check your connection",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    popupWebView?.let { popup ->
        WebViewPopupDialog(
            webView = popup,
            onDismiss = { popupWebView = null },
        )
    }
}
