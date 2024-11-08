package com.james.tronwallet

import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.internal.LinkedTreeMap
import java.lang.reflect.InvocationTargetException

const val TRONMainNet: String = "https://api.trongrid.io"
const val TRONNileNet: String = "https://nile.trongrid.io"
const val TRONApiKey: String = "188434ac-470f-494e-8241-830ed5cb00fc"

public class TronWeb(context: Context, _webView: WebView) {
    private val webView = _webView
    var isGenerateTronWebInstanceSuccess: Boolean = false
    private var bridge = WebViewJavascriptBridge(_context = context,_webView = webView)
    var onCompleted = { _: Boolean,error:String  -> }
    private var showLog: Boolean = false
    init {
        setAllowUniversalAccessFromFileURLs(webView)
    }
    public fun setup(showLog: Boolean = true, privateKey: String, apiKey: String = TRONApiKey, node: String = TRONNileNet, onCompleted: (Boolean,String) -> Unit) {
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
                val state =  map!!["state"] as Boolean
                val error =  map["error"] as String
                if (state) {
                    isGenerateTronWebInstanceSuccess = true
                    onCompleted(true,"")
                }  else {
                    isGenerateTronWebInstanceSuccess = false
                    onCompleted(false,error)
                }
            }
        })
    }
    public fun trxTransferWithRemark(remark: String,
                                     toAddress: String,
                                     amount: String,onCompleted: (Boolean,String,String) -> Unit) {
        val data = java.util.HashMap<String, Any>()
        data["remark"] = remark
        data["toAddress"] = toAddress
        data["amount"] = amount
        bridge.call("trxTransferWithRemark", data, object : Callback {
            override fun call(map: HashMap<String, Any>?){
                if (showLog) {
                    println(map)
                }
                var state =  map!!["result"]
                if (state == null) {
                    state = false
                }
                if (state as Boolean){
                    val txid = map["txid"] as String
                    onCompleted(state,txid,"")
                } else {
                    val code =  map!!["code"]
                    if (code == null){
                        val error = map["error"] as String
                        onCompleted(false,"",error)
                    } else {
                        val txid = map["txid"] as String
                        onCompleted(false,txid,code as String)
                    }
                }
            }
        })
    }
    public fun trxTransferWithOutRemark(toAddress: String,
                                        amount: String,onCompleted: (Boolean,String,String) -> Unit) {
        val data = java.util.HashMap<String, Any>()
        data["toAddress"] = toAddress
        data["amount"] = amount
        bridge.call("trxTransfer", data, object : Callback {
            override fun call(map: HashMap<String, Any>?){
                if (showLog) {
                    println(map)
                }
                var state =  map!!["result"]
                if (state == null) {
                    state = false
                }
                if (state as Boolean){
                    val txid = map["txid"] as String
                    onCompleted(state,txid,"")
                } else {
                    val code =  map!!["code"]
                    if (code == null){
                        val error = map["error"] as String
                        onCompleted(false,"",error)
                    } else {
                        val txid = map["txid"] as String
                        onCompleted(false,txid,code as String)
                    }
                }
            }
        })
    }
    public fun trc20TokenTransfer(toAddress: String,
                                  trc20ContractAddress: String,
                                  amount: String,
                                  remark: String,
                                  feeLimit: String = "100000000",onCompleted: (Boolean,String,String) -> Unit) {
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
                var state =  map!!["result"]
                if (state == null) {
                    state = false
                }
                if (state as Boolean){
                    val txid = map["txid"] as String
                    onCompleted(state,txid,"")
                } else {
                    val code =  map!!["code"]
                    if (code == null){
                        val error = map["error"] as String
                        onCompleted(false,"",error)
                    } else {
                        val txid = map["txid"] as String
                        onCompleted(false,txid,code as String)
                    }
                }
            }
        })
    }
    public fun getTRXBalance(address: String, onCompleted: (Boolean, String,String) -> Unit ){
        val data = java.util.HashMap<String, Any>()
        data["address"] = address
        bridge.call("getTRXBalance", data, object : Callback {
            override fun call(map: HashMap<String, Any>?){
                if (showLog) {
                    println(map)
                }
                val state =  map!!["state"] as Boolean
                if (state) {
                    val balance =  map["result"] as String
                    onCompleted(state,balance,"")
                } else {
                    val error =  map["error"] as String
                    onCompleted(false,"",error)
                }
            }
        })
    }
    public fun getTRC20TokenBalance(address: String,
                                    trc20ContractAddress: String,
                                    decimalPoints: Double,
                                    onCompleted: (Boolean, String,String) -> Unit ){
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
                if (state) {
                    val balance =  map["result"] as String
                    onCompleted(state,balance,"")
                } else {
                    val error =  map["error"] as String
                    onCompleted(false,"",error)
                }
            }
        })
    }
    //  trc20代幣轉帳estimateEnergy
    public fun getFeeEstimate(url:String,
                              toAddress: String,
                                    trc20ContractAddress: String,
                              amount: String,
                              onCompleted: (Boolean,Double,Double,String) -> Unit ){
        val data = java.util.HashMap<String, Any>()
        data["toAddress"] = toAddress
        data["url"] = url
        data["contractAddress"] = trc20ContractAddress
        data["amount"] = amount
        bridge.call("estimateEnergy", data, object : Callback {
            override fun call(map: HashMap<String, Any>?){
                if (showLog) {
                    println(map)
                }
                val state =  map!!["state"] as Boolean
                if (state) {
                    val energyUsed =  map["energy_used"] as Double
                    val energyFee =  map["energyFee"] as Double
                    onCompleted(state,energyUsed,energyFee,"")
                } else {
                    val error =  map["error"] as String
                    onCompleted(false,0.0,0.0,error)
                }
            }
        })
    }
    // trx轉帳estimate Fee
    public fun estimateTRXTransferFee(
                              toAddress: String,
                              note: String = "",
                              amount: String,
                              onCompleted: (Boolean,LinkedTreeMap<String, Any>,LinkedTreeMap<String, Any>,String) -> Unit ){
        val data = java.util.HashMap<String, Any>()
        data["toAddress"] = toAddress
        data["note"] = note
        data["amount"] = amount
        bridge.call("estimateTRXFee", data, object : Callback {
            override fun call(map: HashMap<String, Any>?){
                if (showLog) {
                    println(map)
                }
                val state =  map!!["state"] as Boolean
                if (state) {
                    val sendAccountResources =  map["sendAccountResources"] as LinkedTreeMap<String, Any>
                    val feeDic =  map["result"] as LinkedTreeMap<String, Any>
                    onCompleted(state,sendAccountResources,feeDic,"")
                } else {
                    val error =  map["error"] as String
                    onCompleted(false,LinkedTreeMap<String, Any>(),LinkedTreeMap<String, Any>(),error)
                }
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
    public fun getAccountResource(address: String, onCompleted: (HashMap<String, Any>) -> Unit ){
        val data = java.util.HashMap<String, Any>()
        data["address"] = address
        bridge.call("getAccountResources", data, object : Callback {
            override fun call(map: HashMap<String, Any>?){
                if (showLog) {
                    println(map)
                }
                onCompleted(map ?: HashMap<String, Any>())
            }
        })
    }
    public fun getChainParameters(onCompleted: (HashMap<String, Any>) -> Unit ){
        val data = java.util.HashMap<String, Any>()
        bridge.call("getChainParameters", data, object : Callback {
            override fun call(map: HashMap<String, Any>?){
                if (showLog) {
                    println(map)
                }
                onCompleted(map ?: HashMap<String, Any>())
            }
        })
    }
    public fun createRandom(onCompleted: (Boolean, String,String,String,String,String) -> Unit ){
        val data = java.util.HashMap<String, Any>()
        bridge.call("createRandom", data, object : Callback {
            override fun call(map: HashMap<String, Any>?){
                if (showLog) {
                    println(map)
                }
                map?.let {
                    val state = it["state"] as Boolean
                    val address = it["address"] as String
                    val mnemonic = it["mnemonic"] as String
                    val privateKey = it["privateKey"] as String
                    val publicKey = it["publicKey"] as String
                    val error = it["error"] as String
                    onCompleted(state, address, privateKey, publicKey, mnemonic, error)
                }
            }
        })
    }

    public fun createAccount(onCompleted: (Boolean, String,String,String,String,String) -> Unit) {
        val data = HashMap<String, Any>()
        bridge.call("createAccount", data, object : Callback {
            override fun call(map: HashMap<String, Any>?) {
                if (showLog) {
                    println(map)
                }
                map?.let {
                    val state = it["state"] as Boolean
                    val base58Address = it["base58Address"] as String
                    val hexAddress = it["hexAddress"] as String
                    val privateKey = it["privateKey"] as String
                    val publicKey = it["publicKey"] as String
                    val error = it["error"] as String
                    onCompleted(state, hexAddress,base58Address, privateKey, publicKey, error)
                }
            }
        })
    }
    public fun importAccountFromMnemonic(mnemonic: String, onCompleted: (Boolean, String, String, String, String) -> Unit) {
        val data = HashMap<String, Any>()
        data["mnemonic"] = mnemonic
        bridge.call("importAccountFromMnemonic", data, object : Callback {
            override fun call(map: HashMap<String, Any>?) {
                if (showLog) {
                    println(map)
                }
                map?.let {
                    val state = it["state"] as Boolean
                    val address = it["address"] as String
                    val privateKey = it["privateKey"] as String
                    val publicKey = it["publicKey"] as String
                    val error = it["error"] as String
                    onCompleted(state, address, privateKey, publicKey, error)
                }
            }
        })
    }
    public fun importAccountFromPrivateKey(privateKey: String, onCompleted: (Boolean, String, String, String) -> Unit) {
        val data = HashMap<String, Any>()
        data["privateKey"] = privateKey
        bridge.call("importAccountFromPrivateKey", data, object : Callback {
            override fun call(map: HashMap<String, Any>?) {
                if (showLog) {
                    println(map)
                }
                map?.let {
                    val state = it["state"] as Boolean
                    val base58 = it["base58"] as String
                    val hex = it["hex"] as String
                    val error = it["error"] as String
                    onCompleted(state, base58, hex, error)
                }
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