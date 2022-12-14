package com.james.tronwallet

import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import java.lang.reflect.InvocationTargetException

const val TRONMainNet: String = "https://api.trongrid.io"
const val TRONNileNet: String = "https://nile.trongrid.io"
const val TRONApiKey: String = "188434ac-470f-494e-8241-830ed5cb00fc"

public class TronWeb(context: Context, _webView: WebView) {
    private val webView = _webView
    var isGenerateTronWebInstanceSuccess: Boolean = false
    private var bridge = WebViewJavascriptBridge(_context = context,_webView = webView)
    var onCompleted = { _: Boolean  -> }
    private var showLog: Boolean = false
    init {
        setAllowUniversalAccessFromFileURLs(webView)
    }
    public fun setup(showLog: Boolean = true, privateKey: String, apiKey: String = TRONApiKey, node: String = TRONNileNet, onCompleted: (Boolean) -> Unit) {
        this.showLog = showLog
        this.onCompleted = onCompleted
        webView.webViewClient = webClient
        if (showLog) {
            bridge.consolePipe = object : ConsolePipe {
                override fun post(string : String){
                    println("Next line is javascript console.log->>>")
                    println(string)
                }
            }
        }
        webView.loadUrl("file:///android_asset/TronIndex.html")
        val handler = object :Handler {
            override fun handler(map: HashMap<String, Any>?, callback: Callback) {
                println("FinishLoad---------->>>")
                generateTronWebInstance(privateKey, apiKey, node)
            }
        }
        bridge.register("FinishLoad",handler)
    }
    public fun generateTronWebInstance(privateKey: String,apiKey:String = TRONApiKey, node: String = TRONNileNet) {
        val data = java.util.HashMap<String, Any>()
        data["privateKey"] = privateKey
        data["node"] = node
        data["apiKey"] = apiKey
        bridge.call("generateTronWebInstance", data, object : Callback {
            override fun call(map: HashMap<String, Any>?){
                val result =  map!!["result"] as String
                if (result == "1") {
                    isGenerateTronWebInstanceSuccess = true
                    onCompleted(true)
                } else {
                    isGenerateTronWebInstanceSuccess = false
                    onCompleted(false)
                }
            }
        })
    }
    public fun trxTransferWithRemark(remark: String,
                                     toAddress: String,
                                     amount: String,onCompleted: (Boolean,String) -> Unit) {
        val data = java.util.HashMap<String, Any>()
        data["remark"] = remark
        data["toAddress"] = toAddress
        data["amount"] = amount
        bridge.call("trxTransferWithRemark", data, object : Callback {
            override fun call(map: HashMap<String, Any>?){
                if (showLog) {
                    println(map)
                }
                val state =  map!!["result"] as Boolean
                val txid = if (!state) "error" else  map["txid"] as String
                onCompleted(state,txid)
            }
        })
    }
    public fun trxTransferWithOutRemark(toAddress: String,
                                        amount: String,onCompleted: (Boolean,String) -> Unit) {
        val data = java.util.HashMap<String, Any>()
        data["toAddress"] = toAddress
        data["amount"] = amount
        bridge.call("trxTransfer", data, object : Callback {
            override fun call(map: HashMap<String, Any>?){
                if (showLog) {
                    println(map)
                }
                val state =  map!!["result"] as Boolean
                val txid = if (!state) "error" else  map["txid"] as String
                onCompleted(state,txid)
            }
        })
    }
    public fun trc20TokenTransfer(toAddress: String,
                                  trc20ContractAddress: String,
                                  amount: String,
                                  remark: String,
                                  feeLimit: String = "100000000",onCompleted: (Boolean,String) -> Unit) {
        val data = java.util.HashMap<String, Any>()
        data["toAddress"] = toAddress
        data["amount"] = amount
        data["trc20ContractAddress"] = trc20ContractAddress
        data["remark"] = remark
        data["amount"] = amount
        data["feeLimit"] = feeLimit
        bridge.call("tokenTransfer", data, object : Callback {
            override fun call(map: HashMap<String, Any>?){
                if (showLog) {
                    println(map)
                }
                val state =  map!!["result"] as Boolean
                val txid = if (!state) "error" else  map["txid"] as String
                onCompleted(state,txid)
            }
        })
    }
    public fun getRTXBalance(address: String, onCompleted: (Boolean, String) -> Unit ){
        val data = java.util.HashMap<String, Any>()
        data["address"] = address
        bridge.call("getTRXBalance", data, object : Callback {
            override fun call(map: HashMap<String, Any>?){
                if (showLog) {
                    println(map)
                }
                val state =  map!!["state"] as Boolean
                val balance = if (!state) "error" else  map["result"] as String
                onCompleted(state,balance)
            }
        })
    }
    public fun getTRC20TokenBalance(address: String,
                                    trc20ContractAddress: String,
                                    decimalPoints: Double,
                                    onCompleted: (Boolean, String) -> Unit ){
        val data = java.util.HashMap<String, Any>()
        data["address"] = address
        data["trc20ContractAddress"] = trc20ContractAddress
        data["decimalPoints"] = decimalPoints
        bridge.call("getTRC20TokenBalance", data, object : Callback {
            override fun call(map: HashMap<String, Any>?){
                if (showLog) {
                    println(map)
                }
                val state =  map!!["state"] as Boolean
                val balance = if (!state) "error" else map["result"] as String
                onCompleted(state,balance)
            }
        })
    }
    public fun getAccount(address: String, onCompleted: (HashMap<String, Any>) -> Unit ){
        val data = java.util.HashMap<String, Any>()
        data["address"] = address
        bridge.call("getAccount", data, object : Callback {
            override fun call(map: HashMap<String, Any>?){
                if (showLog) {
                    println(map)
                }
                onCompleted(map ?: HashMap<String, Any>())
            }
        })
    }
    public fun tronWebResetPrivateKey(newPrivateKey: String, onCompleted: (Boolean) -> Unit ){
        val data = java.util.HashMap<String, Any>()
        data["privateKey"] = newPrivateKey
        bridge.call("resetPrivateKey", data, object : Callback {
            override fun call(map: HashMap<String, Any>?){
                if (showLog) {
                    println(map)
                }
                val result =  map!!["result"] as Boolean
                onCompleted(result)
            }
        })
    }
    private val webClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            println("shouldOverrideUrlLoading")
            return false
        }
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            println("onPageStarted")
            bridge.injectJavascript()
        }
        override fun onPageFinished(view: WebView?, url: String?) {
            println("onPageFinished")
        }
    }
    //Allow Cross Domain
    private fun setAllowUniversalAccessFromFileURLs(webView: WebView) {
        try {
            val clazz: Class<*> = webView.settings.javaClass
            val method = clazz.getMethod(
                "setAllowUniversalAccessFromFileURLs", Boolean::class.javaPrimitiveType
            )
            method.invoke(webView.settings, true)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }
}