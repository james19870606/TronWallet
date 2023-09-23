package com.james.tronwallet

import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CreateRandomActivity: AppCompatActivity()  {
    private var title: TextView? = null
    private var walletDetail: EditText? = null
    private var createRandomBtn: Button? = null
    private var mWebView: WebView? = null
    private var tronweb:TronWeb? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_random)
        setupContent()
    }
    private fun setupContent() {
        title = findViewById(R.id.title)
        walletDetail = findViewById(R.id.wallet_detail)
        createRandomBtn = findViewById(R.id.btn_create_random)
        mWebView =  findViewById(R.id.webView)
        tronweb = TronWeb(this, _webView = mWebView!!)
        createRandomBtn?.setOnClickListener{
            createRandom()
        }
    }
    private fun createRandom() {
        val onCompleted = {result : Boolean ->
            println("onCompleted------->>>>>")
            println(result)
            createRandomAction()
        }
        if (tronweb?.isGenerateTronWebInstanceSuccess == false) {
            tronweb?.setup(true, "01",onCompleted = onCompleted)
        }  else {
            createRandomAction()
        }
    }

    private fun updateWalletDetails(state: Boolean, address: String, mnemonic: String, privateKey: String, publicKey: String, error: String) {
        runOnUiThread {
            val text = """
                address: $address
    
                mnemonic: $mnemonic
    
                privateKey: $privateKey
    
                publicKey: $publicKey
            """
            walletDetail?.setText(if (state) text else error)
        }
    }

    private fun createRandomAction() {
        val onCompleted = { state: Boolean, address: String, privateKey: String, publicKey: String, mnemonic: String, error: String ->
            updateWalletDetails(state, address, mnemonic, privateKey, publicKey, error)
        }
        tronweb?.createRandom(onCompleted = onCompleted)
    }
}