package com.james.sdk.tronweb.bridge

import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.webkit.WebViewCompat

/**
 * WebViewJavascriptBridge - Main interface class that encapsulates Android WebView integration
 */
class WebViewJavascriptBridge(
    private val webView: WebView,
    private val otherJSCode: String = "",
    private val isHookConsole: Boolean = true
) {

    private val base = WebViewJavascriptBridgeBase()
    var consolePipeClosure: ((Any?) -> Unit)? = null

    init {
        // Set evaluateJavascript callback
        base.setEvaluateJavascript { javascript, callback ->
            webView.evaluateJavascript(javascript) { result ->
                callback?.invoke(result)
            }
        }

        // Add JavaScript interface
        addJavaScriptInterface()

        // Inject JavaScript code (using DOCUMENT_START_SCRIPT only)
        injectJavascriptFile()
    }

    /**
     * Add JavaScript interface to allow JS to call Native
     */
    private fun addJavaScriptInterface() {
        // Create AndroidBridge object for JS to call Native
        webView.addJavascriptInterface(AndroidBridge(this), "AndroidBridge")
    }

    /**
     * Inject JavaScript code
     * For Android 7.0+, DOCUMENT_START_SCRIPT is widely supported, use it directly
     */
    private fun injectJavascriptFile() {
        val bridgeJS = JavascriptCode.bridge()
        val hookConsoleJS = if (isHookConsole) JavascriptCode.hookConsole() else ""
        var finalJS = "$bridgeJS\n$hookConsoleJS"

        // Add additional JS code if provided
        if (otherJSCode.isNotEmpty()) {
            finalJS = "$finalJS\n$otherJSCode"
        }

        // Use WebViewCompat.addDocumentStartJavaScript to inject at document start
        // All WebView on Android 7.0+ support this feature
        WebViewCompat.addDocumentStartJavaScript(
            webView,
            finalJS,
            setOf("*")  // Allow all origins (including local files)
        )
    }

    /**
     * Reset Bridge
     */
    fun reset() {
        base.reset()
    }

    /**
     * Register handler for JS to call
     */
    fun register(handlerName: String, handler: BridgeHandler) {
        base.register(handlerName, handler)
    }

    /**
     * Remove handler
     */
    fun remove(handlerName: String): BridgeHandler? {
        return base.remove(handlerName)
    }

    /**
     * Call JS handler
     */
    fun call(handlerName: String, data: Any? = null, callback: BridgeCallback? = null) {
        base.send(handlerName, data, callback)
    }

    /**
     * Handle message from JS
     */
    internal fun handleMessage(messageJSON: String) {
        base.flush(messageJSON)
    }

    /**
     * Handle console.log from JS
     */
    internal fun handleConsoleLog(message: String) {
        consolePipeClosure?.invoke(message)
    }

    /**
     * AndroidBridge - JavaScript interface class
     * JS can call these methods through @JavascriptInterface annotation
     */
    class AndroidBridge(private val bridge: WebViewJavascriptBridge) {

        @JavascriptInterface
        fun postMessage(messageJSON: String) {
            // JS calls this method to send message to Native
            bridge.handleMessage(messageJSON)
        }

        @JavascriptInterface
        fun consoleLog(message: String) {
            // JS calls this method to send console.log to Native
            bridge.handleConsoleLog(message)
        }
    }
}
