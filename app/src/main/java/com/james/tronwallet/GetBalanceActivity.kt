package com.james.tronwallet

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GetBalanceActivity : AppCompatActivity() {
    private var title: TextView? = null
    private var balance: TextView? = null
    private var address: EditText? = null
    private var trc20Address: EditText? = null
    private var getBalanceBtn: Button? = null
    private var tronweb:TronWeb? = null
    private var mWebView: WebView? = null
    private var action: String = ""
    private var position: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.balance_layout)
        setupContent()
        getData()

    }
    private fun setupContent() {
        title = findViewById(R.id.title)
        balance = findViewById(R.id.balance)
        address = findViewById(R.id.address)
        trc20Address = findViewById(R.id.trc20Address)
        getBalanceBtn = findViewById(R.id.btn_getBalance)
        mWebView =  findViewById(R.id.webView)
        tronweb = TronWeb(this, _webView = mWebView!!)
        getBalanceBtn?.setOnClickListener{
            getBalance()
        }
    }
    private fun getBalance() {
        val onCompleted = {result : Boolean ->
            println("onCompleted------->>>>>")
            println(result)
            if (action == "getTRXBalance") getTRXBalance() else getTRC20Balance()
        }
        val privateKey = "01"
        val node = if(position == 0) TRONMainNet else TRONNileNet
        if (tronweb?.isGenerateTronWebInstanceSuccess == false) {
            tronweb?.setup(true, privateKey, node = node,onCompleted = onCompleted)
        }  else  {
            if (action == "getTRXBalance") getTRXBalance() else  getTRC20Balance()
        }
    }
    @SuppressLint("SetTextI18n")
    private fun getTRXBalance() {
        val address = address?.text.toString()
        if (address.isNotEmpty()) {
            val onCompleted = {state : Boolean, amount: String ,error:String->
                this.runOnUiThread {
                    if (state) {
                        val titleTip = if(position == 0) "主網餘額：" else "Nile測試網餘額： "
                        balance?.text = titleTip + amount
                    } else {
                        balance?.text = error
                    }
                }
            }
            tronweb?.getTRXBalance(
                address ,
                onCompleted = onCompleted)
        }
    }
    @SuppressLint("SetTextI18n")
    private fun getTRC20Balance() {
        val address = address?.text.toString()
        val trc20address = trc20Address?.text.toString()
        if (address.isNotEmpty() && trc20address.isNotEmpty()) {
            val onCompleted = {state : Boolean, amount: String,error:String ->
                this.runOnUiThread {
                    if (state) {
                        val titleTip = if(position == 0) "主網餘額：" else "Nile測試網餘額： "
                        balance?.text = titleTip + amount
                    } else {
                        balance?.text = error
                    }
                }
            }
            tronweb?.getTRC20TokenBalance(
                address ,
                trc20address,
                6.0,
                onCompleted = onCompleted)
        }
    }
    private fun getData() {
        //接收传值
        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            action = bundle.getString("action") ?: ""
            position = bundle.getInt("position") ?: 0
            println("this position:$position  action:$action")
            title?.text = if (position == 0) "主網獲取餘額" else "Nile測試網獲取餘額"
            if (action == "getTRXBalance") {
                trc20Address?.setVisibility(View.GONE)
            }
        }
    }
}