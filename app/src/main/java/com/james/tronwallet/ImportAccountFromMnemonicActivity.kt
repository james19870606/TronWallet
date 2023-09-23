package com.james.tronwallet

import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ImportAccountFromMnemonicActivity: AppCompatActivity() {
    private var title: TextView? = null

    private var mnemonicEditText: EditText? = null
    private var walletDetail: EditText? = null
    private var importAccountFromMnemonicBtn: Button? = null
    private var mWebView: WebView? = null
    private var tronweb:TronWeb? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.import_account_from_mnemonic)
        setupContent()
    }
    private fun setupContent() {
        title = findViewById(R.id.title)
        mnemonicEditText = findViewById(R.id.mnemonic)
        walletDetail = findViewById(R.id.wallet_detail)
        importAccountFromMnemonicBtn = findViewById(R.id.btn_import_account_from_mnemonic)
        mWebView =  findViewById(R.id.webView)
        tronweb = TronWeb(this, _webView = mWebView!!)
        importAccountFromMnemonicBtn?.setOnClickListener{
            importAccountFromMnemonic()
        }
    }
    private fun importAccountFromMnemonic() {
        val onCompleted = {result : Boolean ->
            println("onCompleted------->>>>>")
            println(result)
            importAccountFromMnemonicAction()
        }
        if (tronweb?.isGenerateTronWebInstanceSuccess == false) {
            tronweb?.setup(true, "01",onCompleted = onCompleted)
        }  else {
            importAccountFromMnemonicAction()
        }
    }

    private fun updateWalletDetails(state: Boolean,
                                    address: String,
                                    privateKey: String,
                                    publicKey: String,
                                    error: String) {
        runOnUiThread {
            val text = """
                address: $address
    
                privateKey: $privateKey
    
                publicKey: $publicKey
            """
            walletDetail?.setText(if (state) text else error)
        }
    }

    private fun importAccountFromMnemonicAction() {
        val mnemonic = mnemonicEditText?.getText().toString();
        val onCompleted = { state: Boolean, address: String, privateKey: String, publicKey: String, error: String ->
            updateWalletDetails(state, address, privateKey, publicKey, error)
        }
        tronweb?.importAccountFromMnemonic(mnemonic, onCompleted = onCompleted)
    }
}