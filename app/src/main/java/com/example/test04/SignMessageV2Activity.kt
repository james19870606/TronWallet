package com.example.test04

import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.james.tronwallet.TronWeb

class SignMessageV2Activity: AppCompatActivity() {
    private var title: TextView? = null

    private var messageEditText: EditText? = null
    private var signDetail: EditText? = null
    private var signMessageV2Btn: Button? = null
    private var mWebView: WebView? = null
    private var tronWeb:TronWeb? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_message_v2)
        setupContent()
    }
    private fun setupContent() {
        title = findViewById(R.id.title)
        messageEditText = findViewById(R.id.message)
        signDetail = findViewById(R.id.sign_detail)
        signMessageV2Btn = findViewById(R.id.btn_import_sign_message_v2)
        mWebView =  findViewById(R.id.webView)
        tronWeb = TronWeb(this, _webView = mWebView!!)
        signMessageV2Btn?.setOnClickListener{
            signMessageV2()
        }
    }
    private fun signMessageV2() {
        val onCompleted = {result : Boolean,error:String ->
            if (result) {
                println("onCompleted------->>>>>")
                println(result)
                signMessageV2Action()
            } else {
                println(error)
            }
        }
        if (tronWeb?.isGenerateTronWebInstanceSuccess == false) {
            tronWeb?.setup(true, "01",onCompleted = onCompleted)
        }  else {
            signMessageV2Action()
        }
    }

    private fun updateSignDetails(state: Boolean,
                                  signature: String,
                                    error: String) {
        runOnUiThread {
            signDetail?.setText(if (state) signature else error)
        }
    }

    private fun signMessageV2Action() {
        val message = messageEditText?.getText().toString();
        val onCompleted = { state: Boolean, signature: String, error: String ->
            updateSignDetails(state, signature, error)
        }
        val p1 = "57f75d7325d8ba0e6882b4be7afb3bb36"
        val p2 = "b34d184d3c58c28439a9b72cc597d86"
        tronWeb?.signMessageV2(message,p1 + p2, onCompleted = onCompleted)
    }
}