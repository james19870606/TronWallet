package com.example.test04

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.james.tronwallet.TronWeb
import com.james.tronwallet.TRONMainNet
import com.james.tronwallet.TRONNileNet
class TransferActivity : AppCompatActivity() {
    private var title: TextView? = null
    private var hashValue: TextView? = null
    private var privateKeyEditText: EditText? = null
    private var receiveEditText: EditText? = null
    private var amountEditText: EditText? = null
    private var remarkEditText: EditText? = null
    private var trc20EditText: EditText? = null
    private var transferBtn: Button? = null
    private var detailBtn: Button? = null
    private var tronweb:TronWeb? = null
    private var mWebView: WebView? = null
    private var action: String = ""
    private var position: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transfer_layout)
        setupContent()
        getData()
        println(TRONMainNet)
    }
    private fun setupContent(){
        trc20EditText  = findViewById(R.id.trc20Address)
        privateKeyEditText = findViewById(R.id.private_key)
        receiveEditText = findViewById(R.id.receive_address)
        amountEditText = findViewById(R.id.amount)
        remarkEditText = findViewById(R.id.remark)
        title = findViewById(R.id.title)
        hashValue = findViewById(R.id.hashValue)
        transferBtn = findViewById(R.id.btn_transfer)
        detailBtn = findViewById(R.id.btn_detail)
        mWebView = findViewById(R.id.webView)
        tronweb = TronWeb(this, _webView = mWebView!!)
        transferBtn?.setOnClickListener{
            transfer()
        }
        detailBtn?.setOnClickListener{
            val hash = hashValue?.text.toString()
            if (hash.length < 20) { return@setOnClickListener}
            var urlString = if(position == 0)  "https://tronscan.org/#/transaction/" else "https://nile.tronscan.org/#/transaction/"
            urlString += hash
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(urlString))
                startActivity(intent)
            } catch (e: Exception) {
                println("当前手机未安装浏览器")
            }
        }
    }
    private fun transfer(){
        val onCompleted = {result : Boolean,error:String ->
            if (result){
                println("onCompleted------->>>>>")
                println(result)
                if (action == "trxTransfer") trxTransfer() else trc20Transfer()
            } else {
                println(error)
            }
        }
        val privateKey = privateKeyEditText?.text.toString()
        val node = if(position == 0) TRONMainNet else TRONNileNet
        if (tronweb?.isGenerateTronWebInstanceSuccess == false) {
            tronweb?.setup(true, privateKey, node = node,onCompleted = onCompleted)
        } else  {
            if (action == "trxTransfer") trxTransfer() else  trc20Transfer()
        }
    }
    private fun trxTransfer() {
        val remark = remarkEditText?.text.toString()
        val toAddress = receiveEditText?.text.toString()
        val amount = amountEditText?.text.toString()
        if (toAddress.isNotEmpty() && amount.isNotEmpty() && remark.isNotEmpty()) {
            val onCompleted = {state : Boolean, txid: String ,error:String->
                this.runOnUiThread {
                    if (state){
                        hashValue?.text = txid
                    } else {
                        hashValue?.text = error
                    }
                }
            }
            tronweb?.trxTransferWithOutRemark(
                toAddress ,
                amount ,
                onCompleted = onCompleted)
        }
    }
    private fun trc20Transfer(){
        val remark = remarkEditText?.text.toString()
        val toAddress = receiveEditText?.text.toString()
        val trc20ContractAddress = trc20EditText?.text.toString()
        val amount = amountEditText?.text.toString()
        val onCompleted = {state : Boolean, txid: String,error:String ->
            this.runOnUiThread {
                if (state){
                    hashValue?.text = txid
                } else {
                    hashValue?.text = error
                }
            }
        }
        tronweb?.trc20TokenTransfer(
            toAddress,
            trc20ContractAddress,
            amount,
            remark,
            onCompleted = onCompleted)
    }

    private fun getData() {
        //接收传值
        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            action = bundle.getString("action") ?: ""
            position = bundle.getInt("position") ?: 0
            println("this position:$position  action:$action")
            title?.text = if (position == 0) "主網轉帳" else "Nile測試網轉帳"
            val trc20ContractAddress =  if (position == 0) "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t" else "TXLAQ63Xg1NAzckPwKHvzw7CSEmLMEqcdj"
            trc20EditText?.setText(trc20ContractAddress)
            if (action == "trxTransfer") {
                trc20EditText?.setVisibility(View.GONE)
            }
        }
    }
}