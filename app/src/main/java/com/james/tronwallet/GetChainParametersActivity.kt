package com.james.tronwallet

import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GetChainParametersActivity : AppCompatActivity() {
    private var title: TextView? = null
    private var accountDetail: TextView? = null
    private var getDetailBtn: Button? = null
    private var tronweb:TronWeb? = null
    private var mWebView: WebView? = null
    private var action: String = ""
    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chain_parameters_layout)
        setupContent()
        getData()
    }
    private fun setupContent() {
        title = findViewById(R.id.title)
        accountDetail = findViewById(R.id.account_detail)
        getDetailBtn = findViewById(R.id.btn_getBalance)
        mWebView =  findViewById(R.id.webView)
        tronweb = TronWeb(this, _webView = mWebView!!)
        getDetailBtn?.setOnClickListener{
            getDetail()
        }
    }
    private fun getDetail() {
        val onCompleted = {result : Boolean,error:String ->
            if(result){
                println("onCompleted------->>>>>")
                println(result)
                getDetailAction()
            }else{
                println(error)
            }
        }
        val privateKey = "01"
        val node = if(position == 0) TRONMainNet else TRONNileNet
        if (tronweb?.isGenerateTronWebInstanceSuccess == false) {
            tronweb?.setup(true, privateKey, node = node,onCompleted = onCompleted)
        }  else  {
            getDetailAction()
        }
    }
    private fun getDetailAction(){
            val onCompleted = {map:HashMap<String, Any> ->
                this.runOnUiThread {
                    accountDetail?.text = map.toString()
                }
            }
            tronweb?.getChainParameters(
                onCompleted = onCompleted)
    }
    private fun getData() {
        //接收传值
        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            action = bundle.getString("action") ?: ""
            position = bundle.getInt("position") ?: 0
            println("this position:$position  action:$action")
            title?.text = if (position == 0) "主網鏈結上參數資訊" else "Nile鏈結上參數資訊"
        }
    }
}