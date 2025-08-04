package com.example.test04

import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.james.tronwallet.TronWeb

class ImportAccountFromPrivateKeyActivity: AppCompatActivity() {
    private var title: TextView? = null

    private var privateKeyEditText: EditText? = null
    private var walletDetail: EditText? = null
    private var importAccountFromPrivateKeyBtn: Button? = null
    private var mWebView: WebView? = null
    private var tronweb:TronWeb? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.import_account_from_private_key)
        setupContent()
    }
    private fun setupContent() {
        title = findViewById(R.id.title)
        privateKeyEditText = findViewById(R.id.private_key)
        walletDetail = findViewById(R.id.wallet_detail)
        importAccountFromPrivateKeyBtn = findViewById(R.id.btn_import_account_from_private_key)
        mWebView =  findViewById(R.id.webView)
        tronweb = TronWeb(this, _webView = mWebView!!)
        importAccountFromPrivateKeyBtn?.setOnClickListener{
            importAccountFromPrivateKey()
        }
    }
    private fun importAccountFromPrivateKey() {
        val onCompleted = {result : Boolean,error:String ->
            if (result) {
                println("onCompleted------->>>>>")
                println(result)
                importAccountFromPrivateKeyAction()
            } else {
                println(error)
            }
        }
        if (tronweb?.isGenerateTronWebInstanceSuccess == false) {
            tronweb?.setup(true, "01",onCompleted = onCompleted)
        }  else {
            importAccountFromPrivateKeyAction()
        }
    }

    private fun updateWalletDetails(state: Boolean,
                                    base58: String,
                                    hex: String,
                                    error: String) {
        runOnUiThread {
            val text = """
                base58: $base58
    
                hex: $hex
            """
            walletDetail?.setText(if (state) text else error)
        }
    }

    private fun importAccountFromPrivateKeyAction() {
        val privateKey = privateKeyEditText?.getText().toString();
        val onCompleted = { state: Boolean,base58: String, hex: String,  error: String ->
            updateWalletDetails(state, base58, hex, error)
        }
        tronweb?.importAccountFromPrivateKey(privateKey, onCompleted = onCompleted)
    }
}