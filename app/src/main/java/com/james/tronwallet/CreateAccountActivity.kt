package com.james.tronwallet

import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CreateAccountActivity: AppCompatActivity()  {
    private var title: TextView? = null
    private var walletDetail: EditText? = null
    private var createAccountBtn: Button? = null
    private var mWebView: WebView? = null
    private var tronweb:TronWeb? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_account)
        setupContent()
    }
    private fun setupContent() {
        title = findViewById(R.id.title)
        walletDetail = findViewById(R.id.wallet_detail)
        createAccountBtn = findViewById(R.id.btn_create_Account)
        mWebView =  findViewById(R.id.webView)
        tronweb = TronWeb(this, _webView = mWebView!!)
        createAccountBtn?.setOnClickListener{
            createAccount()
        }
    }
    private fun createAccount() {
        val onCompleted = {result : Boolean ->
            println("onCompleted------->>>>>")
            println(result)
            createAccountAction()
        }
        if (tronweb?.isGenerateTronWebInstanceSuccess == false) {
            tronweb?.setup(true, "01",onCompleted = onCompleted)
        }  else {
            createAccountAction()
        }
    }

    private fun updateWalletDetails(state: Boolean,
                                    hexAddress: String,
                                    base58Address: String,
                                    privateKey: String,
                                    publicKey: String,
                                    error: String) {
        runOnUiThread {
            val text = """
                hexAddress: $hexAddress
    
                base58Address: $base58Address
    
                privateKey: $privateKey
    
                publicKey: $publicKey
            """
            walletDetail?.setText(if (state) text else error)
        }
    }

    private fun createAccountAction() {
        val onCompleted = { state: Boolean, hexAddress: String, base58Address: String, privateKey: String, publicKey: String, error: String ->
            updateWalletDetails(state, hexAddress, base58Address, privateKey, publicKey, error)
        }
        tronweb?.createAccount(onCompleted = onCompleted)
    }
}