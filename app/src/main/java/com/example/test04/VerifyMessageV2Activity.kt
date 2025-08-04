package com.example.test04

import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.james.tronwallet.TronWeb

class VerifyMessageV2Activity: AppCompatActivity() {
    private var title: TextView? = null
    
    private var signatureEditText: EditText? = null
    private var verifyDetail: EditText? = null
    private var verifyMessageV2Btn: Button? = null
    private var mWebView: WebView? = null
    private var tronWeb:TronWeb? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verify_message_v2)
        setupContent()
    }
    private fun setupContent() {
        title = findViewById(R.id.title)
        signatureEditText = findViewById(R.id.signature)
        verifyDetail = findViewById(R.id.verify_detail)
        verifyMessageV2Btn = findViewById(R.id.btn_verify_message_v2)
        mWebView =  findViewById(R.id.webView)
        tronWeb = TronWeb(this, _webView = mWebView!!)
        verifyMessageV2Btn?.setOnClickListener{
            verifyMessageV2()
        }
    }
    private fun verifyMessageV2() {
        val onCompleted = {result : Boolean,error:String ->
            if (result) {
                println("onCompleted------->>>>>")
                println(result)
                verifyMessageV2Action()
            } else {
                println(error)
            }
        }
        if (tronWeb?.isGenerateTronWebInstanceSuccess == false) {
            tronWeb?.setup(true, "01",onCompleted = onCompleted)
        }  else {
            verifyMessageV2Action()
        }
    }
    private fun updateVerifyDetails(state: Boolean,
                                    base58Address: String,
                                    error: String) {
        runOnUiThread {
            verifyDetail?.setText(if (state) base58Address else error)
        }
    }

    private fun verifyMessageV2Action() {
        val signature = signatureEditText?.getText().toString();
        val onCompleted = { state: Boolean,base58Address: String,  error: String ->
            updateVerifyDetails(state, base58Address, error)
        }
        tronWeb?.verifyMessageV2("hello world",signature, onCompleted = onCompleted)
    }
}