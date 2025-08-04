package com.example.test04

import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.james.tronwallet.TronWeb

class ResetTronWebPrivateKeyActivity: AppCompatActivity() {
    private var title: TextView? = null
    private var resetBtn: Button? = null
    private var resetResult: TextView? = null
    private var privateKeyEditText: EditText? = null
    private var tronweb:TronWeb? = null
    private var mWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reset_tronweb_private_key)
        setupContent()
        setupTronWeb()
    }
    private fun setupContent() {
        title = findViewById(R.id.title)
        resetBtn = findViewById(R.id.btn_reset)
        resetResult = findViewById(R.id.reset_result)
        privateKeyEditText = findViewById(R.id.private_key)
        mWebView = findViewById(R.id.webView)
        tronweb = TronWeb(this, _webView = mWebView!!)
        resetBtn?.setOnClickListener{
            resetPrivateKey()
        }
    }
    private fun setupTronWeb(){
        val onCompleted = {result : Boolean,error:String ->
            if (result){
                println("onCompleted------->>>>>")
                println(result)
            } else {
                println(error)
            }
        }
        val privateKey = "01"
        tronweb?.setup(true, privateKey,onCompleted = onCompleted)
    }

    private fun resetPrivateKey(){
        val onCompleted = {result : Boolean ->
            println(result)
            if (result) {
                println("重置成功")
            } else {
                println("重置失敗")
            }
        }
        val newPrivateKey =  privateKeyEditText?.text.toString()
        tronweb?.tronWebResetPrivateKey(newPrivateKey,onCompleted)
    }
}