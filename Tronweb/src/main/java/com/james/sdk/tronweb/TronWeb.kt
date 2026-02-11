package com.james.sdk.tronweb

import android.content.Context
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import com.james.sdk.tronweb.bridge.WebViewJavascriptBridge
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * TronWeb - Main SDK class for TRON blockchain operations on Android.
 * Provides both callback-based and Coroutine-based (suspend) asynchronous methods.
 * Integrated with TronWeb.js via WebViewJavascriptBridge.
 */
class TronWeb(private val context: Context) {

    companion object {
        private const val TAG = "TronWeb-SDK"
        const val TRON_MAINNET = "https://api.trongrid.io"
        const val TRON_NILE_NET = "https://nile.trongrid.io"
        const val DEFAULT_API_KEY = "188434ac-470f-494e-8241-830ed5cb00fc"
    }

    private var webView: WebView? = null
    private var bridge: WebViewJavascriptBridge? = null
    
    /**
     * Whether the TronWeb instance is successfully initialized in JS
     */
    var isInitialized: Boolean = false
        private set

    /**
     * Current network node URL
     */
    var currentNode: String = TRON_NILE_NET
        private set

    /**
     * Whether to show logs in Logcat
     */
    var showLog: Boolean = false

    init {
        setupWebView()
    }

    private fun setupWebView() {
        webView = WebView(context).apply {
            @Suppress("SetJavaScriptEnabled")
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            @Suppress("DEPRECATION")
            settings.allowFileAccessFromFileURLs = true
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (showLog) Log.d(TAG, "WebView finished loading: $url")
                }
            }
        }
        
        // Initialize the bridge
        bridge = WebViewJavascriptBridge(webView!!, isHookConsole = true)
        bridge?.consolePipeClosure = { message ->
            if (showLog) Log.d("TronWeb-JS", message.toString())
        }
    }

    /**
     * Initialize the underlying TronWeb.js instance.
     */
    fun setup(
        privateKey: String? = "",
        apiKey: String? = DEFAULT_API_KEY,
        node: String = TRON_NILE_NET,
        onCompleted: ((Boolean, String?) -> Unit)? = null
    ) {
        this.currentNode = node
        
        // Register handler for JS initialization signal
        bridge?.register("FinishLoad") { _, callback ->
            if (showLog) Log.d(TAG, "JS Bridge ready, initializing TronWeb instance...")
            
            val params = mapOf(
                "privateKey" to privateKey,
                "node" to node,
                "apiKey" to apiKey
            )
            
            bridge?.call("generateTronWebInstance", params) { response ->
                val res = response as? Map<*, *>
                val state = res?.get("state") as? Boolean ?: false
                if (state) {
                    isInitialized = true
                    onCompleted?.invoke(true, null)
                } else {
                    val error = res?.get("error") as? String ?: "Unknown initialization error"
                    onCompleted?.invoke(false, error)
                }
                callback?.invoke(mapOf("status" to "received"))
            }
        }
        
        // Load the bundled assets
        webView?.loadUrl("file:///android_asset/index.html")
    }

    /**
     * Async version of setup
     */
    suspend fun setupAsync(
        privateKey: String? = "",
        apiKey: String? = DEFAULT_API_KEY,
        node: String = TRON_NILE_NET
    ): Pair<Boolean, String?> = suspendCancellableCoroutine { continuation ->
        setup(privateKey, apiKey, node) { success, error ->
            continuation.resume(Pair(success, error))
        }
    }

    // MARK: - Wallet Management

    fun createRandom(wordCount: Int = 12, language: String = "english", completion: (Map<String, Any>?) -> Unit) {
        val params = mapOf("wordCount" to wordCount, "language" to language)
        bridge?.call("createRandom", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun createRandomAsync(wordCount: Int = 12, language: String = "english"): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        createRandom(wordCount, language) { response ->
            continuation.resume(response)
        }
    }

    fun importAccountFromMnemonic(mnemonic: String, completion: (Map<String, Any>?) -> Unit) {
        val params = mapOf("mnemonic" to mnemonic)
        bridge?.call("importAccountFromMnemonic", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun importAccountFromMnemonicAsync(mnemonic: String): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        importAccountFromMnemonic(mnemonic) { response ->
            continuation.resume(response)
        }
    }

    fun importAccountFromPrivateKey(privateKey: String, completion: (Map<String, Any>?) -> Unit) {
        val params = mapOf("privateKey" to privateKey)
        bridge?.call("importAccountFromPrivateKey", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun importAccountFromPrivateKeyAsync(privateKey: String): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        importAccountFromPrivateKey(privateKey) { response ->
            continuation.resume(response)
        }
    }

    fun resetTronWebPrivateKey(privateKey: String, completion: (Map<String, Any>?) -> Unit) {
        val params = mapOf("privateKey" to privateKey)
        bridge?.call("resetTronWebPrivateKey", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun resetTronWebPrivateKeyAsync(privateKey: String): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        resetTronWebPrivateKey(privateKey) { response ->
            continuation.resume(response)
        }
    }

    fun createMultiSigAddress(ownerAddress: String, owners: List<String>, required: Int, privateKey: String, completion: (Map<String, Any>?) -> Unit) {
        val params = mapOf(
            "ownerAddress" to ownerAddress,
            "owners" to owners,
            "required" to required,
            "privateKey" to privateKey
        )
        bridge?.call("createMultiSigAddress", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun createMultiSigAddressAsync(ownerAddress: String, owners: List<String>, required: Int, privateKey: String): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        createMultiSigAddress(ownerAddress, owners, required, privateKey) { response ->
            continuation.resume(response)
        }
    }

    // MARK: - Account Query

    fun getAccount(address: String, completion: (Map<String, Any>?) -> Unit) {
        val params = mapOf("address" to address)
        bridge?.call("getAccount", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun getAccountAsync(address: String): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        getAccount(address) { response ->
            continuation.resume(response)
        }
    }

    fun getTRXBalance(address: String, completion: (Map<String, Any>?) -> Unit) {
        val params = mapOf("address" to address)
        bridge?.call("getTRXBalance", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun getTRXBalanceAsync(address: String): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        getTRXBalance(address) { response ->
            continuation.resume(response)
        }
    }

    fun getTRC20TokenBalance(contractAddress: String, address: String, completion: (Map<String, Any>?) -> Unit) {
        val params = mapOf("contractAddress" to contractAddress, "address" to address)
        bridge?.call("getTRC20TokenBalance", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun getTRC20TokenBalanceAsync(contractAddress: String, address: String): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        getTRC20TokenBalance(contractAddress, address) { response ->
            continuation.resume(response)
        }
    }

    fun getAccountResources(address: String, completion: (Map<String, Any>?) -> Unit) {
        val params = mapOf("address" to address)
        bridge?.call("getAccountResources", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun getAccountResourcesAsync(address: String): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        getAccountResources(address) { response ->
            continuation.resume(response)
        }
    }

    fun getChainParameters(completion: (Map<String, Any>?) -> Unit) {
        bridge?.call("getChainParameters", null) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun getChainParametersAsync(): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        getChainParameters { response ->
            continuation.resume(response)
        }
    }

    // MARK: - Message Signing

    fun signMessageV2(message: String, privateKey: String, completion: (Map<String, Any>?) -> Unit) {
        val params = mapOf("message" to message, "privateKey" to privateKey)
        bridge?.call("signMessageV2", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun signMessageV2Async(message: String, privateKey: String): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        signMessageV2(message, privateKey) { response ->
            continuation.resume(response)
        }
    }

    fun verifyMessageV2(message: String, signature: String, address: String, completion: (Map<String, Any>?) -> Unit) {
        val params = mapOf("message" to message, "signature" to signature, "address" to address)
        bridge?.call("verifyMessageV2", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun verifyMessageV2Async(message: String, signature: String, address: String): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        verifyMessageV2(message, signature, address) { response ->
            continuation.resume(response)
        }
    }

    // MARK: - Transaction Operations

    fun trxTransfer(toAddress: String, amount: Double, privateKey: String, remark: String? = null, completion: (Map<String, Any>?) -> Unit) {
        val params = mutableMapOf<String, Any>(
            "toAddress" to toAddress,
            "amount" to amount,
            "privateKey" to privateKey
        )
        remark?.let { params["remark"] = it }
        bridge?.call("trxTransfer", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun trxTransferAsync(toAddress: String, amount: Double, privateKey: String, remark: String? = null): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        trxTransfer(toAddress, amount, privateKey, remark) { response ->
            continuation.resume(response)
        }
    }

    fun trc20Transfer(contractAddress: String, toAddress: String, amount: Double, privateKey: String, remark: String? = null, completion: (Map<String, Any>?) -> Unit) {
        val params = mutableMapOf<String, Any>(
            "contractAddress" to contractAddress,
            "toAddress" to toAddress,
            "amount" to amount,
            "privateKey" to privateKey
        )
        remark?.let { params["remark"] = it }
        bridge?.call("trc20Transfer", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun trc20TransferAsync(contractAddress: String, toAddress: String, amount: Double, privateKey: String, remark: String? = null): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        trc20Transfer(contractAddress, toAddress, amount, privateKey, remark) { response ->
            continuation.resume(response)
        }
    }

    fun estimateTrxFee(toAddress: String, amount: Double, fromAddress: String, remark: String? = null, completion: (Map<String, Any>?) -> Unit) {
        val params = mutableMapOf<String, Any>(
            "toAddress" to toAddress,
            "amount" to amount,
            "fromAddress" to fromAddress
        )
        remark?.let { params["remark"] = it }
        bridge?.call("estimateTrxFee", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun estimateTrxFeeAsync(toAddress: String, amount: Double, fromAddress: String, remark: String? = null): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        estimateTrxFee(toAddress, amount, fromAddress, remark) { response ->
            continuation.resume(response)
        }
    }

    fun estimateTrc20Fee(contractAddress: String, toAddress: String, amount: Double, fromAddress: String, remark: String? = null, completion: (Map<String, Any>?) -> Unit) {
        val params = mutableMapOf<String, Any>(
            "contractAddress" to contractAddress,
            "toAddress" to toAddress,
            "amount" to amount,
            "fromAddress" to fromAddress
        )
        remark?.let { params["remark"] = it }
        bridge?.call("estimateTrc20Fee", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun estimateTrc20FeeAsync(contractAddress: String, toAddress: String, amount: Double, fromAddress: String, remark: String? = null): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        estimateTrc20Fee(contractAddress, toAddress, amount, fromAddress, remark) { response ->
            continuation.resume(response)
        }
    }

    // MARK: - Multi-Sig Operations

    fun multiSigTrxTransfer(fromAddress: String, toAddress: String, amount: Double, privateKeys: List<String>, permissionId: Int = 2, remark: String? = null, completion: (Map<String, Any>?) -> Unit) {
        val params = mutableMapOf<String, Any>(
            "fromAddress" to fromAddress,
            "toAddress" to toAddress,
            "amount" to amount,
            "privateKeys" to privateKeys,
            "permissionId" to permissionId
        )
        remark?.let { params["remark"] = it }
        bridge?.call("multiSigTrxTransfer", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun multiSigTrxTransferAsync(fromAddress: String, toAddress: String, amount: Double, privateKeys: List<String>, permissionId: Int = 2, remark: String? = null): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        multiSigTrxTransfer(fromAddress, toAddress, amount, privateKeys, permissionId, remark) { response ->
            continuation.resume(response)
        }
    }

    fun estimateMultiSigTrxFee(fromAddress: String, toAddress: String, amount: Double, privateKeysCount: Int, permissionId: Int = 2, remark: String? = null, completion: (Map<String, Any>?) -> Unit) {
        val params = mutableMapOf<String, Any>(
            "fromAddress" to fromAddress,
            "toAddress" to toAddress,
            "amount" to amount,
            "privateKeysCount" to privateKeysCount,
            "permissionId" to permissionId
        )
        remark?.let { params["remark"] = it }
        bridge?.call("estimateMultiSigTrxFee", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun estimateMultiSigTrxFeeAsync(fromAddress: String, toAddress: String, amount: Double, privateKeysCount: Int, permissionId: Int = 2, remark: String? = null): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        estimateMultiSigTrxFee(fromAddress, toAddress, amount, privateKeysCount, permissionId, remark) { response ->
            continuation.resume(response)
        }
    }

    fun multiSigTrc20Transfer(contractAddress: String, fromAddress: String, toAddress: String, amount: Double, privateKeys: List<String>, permissionId: Int = 2, remark: String? = null, completion: (Map<String, Any>?) -> Unit) {
        val params = mutableMapOf<String, Any>(
            "contractAddress" to contractAddress,
            "fromAddress" to fromAddress,
            "toAddress" to toAddress,
            "amount" to amount,
            "privateKeys" to privateKeys,
            "permissionId" to permissionId
        )
        remark?.let { params["remark"] = it }
        bridge?.call("multiSigTrc20Transfer", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun multiSigTrc20TransferAsync(contractAddress: String, fromAddress: String, toAddress: String, amount: Double, privateKeys: List<String>, permissionId: Int = 2, remark: String? = null): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        multiSigTrc20Transfer(contractAddress, fromAddress, toAddress, amount, privateKeys, permissionId, remark) { response ->
            continuation.resume(response)
        }
    }

    fun estimateMultiSigTrc20Fee(contractAddress: String, fromAddress: String, toAddress: String, amount: Double, privateKeysCount: Int, permissionId: Int = 2, remark: String? = null, completion: (Map<String, Any>?) -> Unit) {
        val params = mutableMapOf<String, Any>(
            "contractAddress" to contractAddress,
            "fromAddress" to fromAddress,
            "toAddress" to toAddress,
            "amount" to amount,
            "privateKeysCount" to privateKeysCount,
            "permissionId" to permissionId
        )
        remark?.let { params["remark"] = it }
        bridge?.call("estimateMultiSigTrc20Fee", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun estimateMultiSigTrc20FeeAsync(contractAddress: String, fromAddress: String, toAddress: String, amount: Double, privateKeysCount: Int, permissionId: Int = 2, remark: String? = null): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        estimateMultiSigTrc20Fee(contractAddress, fromAddress, toAddress, amount, privateKeysCount, permissionId, remark) { response ->
            continuation.resume(response)
        }
    }

    // MARK: - Resource Staking (Stake 2.0)

    fun freezeBalance(amount: Double, resourceType: String, privateKey: String, completion: (Map<String, Any>?) -> Unit) {
        val params = mapOf("amount" to amount, "resourceType" to resourceType, "privateKey" to privateKey)
        bridge?.call("freezeBalance", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun freezeBalanceAsync(amount: Double, resourceType: String, privateKey: String): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        freezeBalance(amount, resourceType, privateKey) { response ->
            continuation.resume(response)
        }
    }

    fun unfreezeBalance(amount: Double, resourceType: String, privateKey: String, completion: (Map<String, Any>?) -> Unit) {
        val params = mapOf("amount" to amount, "resourceType" to resourceType, "privateKey" to privateKey)
        bridge?.call("unfreezeBalance", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun unfreezeBalanceAsync(amount: Double, resourceType: String, privateKey: String): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        unfreezeBalance(amount, resourceType, privateKey) { response ->
            continuation.resume(response)
        }
    }

    fun delegateResource(amount: Double, resourceType: String, receiverAddress: String, privateKey: String, completion: (Map<String, Any>?) -> Unit) {
        val params = mapOf("amount" to amount, "resourceType" to resourceType, "receiverAddress" to receiverAddress, "privateKey" to privateKey)
        bridge?.call("delegateResource", params) { response ->
            @Suppress("UNCHECKED_CAST")
            completion(response as? Map<String, Any>)
        }
    }

    suspend fun delegateResourceAsync(amount: Double, resourceType: String, receiverAddress: String, privateKey: String): Map<String, Any>? = suspendCancellableCoroutine { continuation ->
        delegateResource(amount, resourceType, receiverAddress, privateKey) { response ->
            continuation.resume(response)
        }
    }
}
