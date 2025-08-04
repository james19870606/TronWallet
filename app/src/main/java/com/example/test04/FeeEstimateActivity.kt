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
import com.google.gson.internal.LinkedTreeMap

import androidx.appcompat.app.AppCompatActivity
import com.james.tronwallet.TronWeb
import com.james.tronwallet.TRONMainNet
import com.james.tronwallet.TRONNileNet
class FeeEstimateActivity : AppCompatActivity() {
    private var title: TextView? = null
    private var hashValue: TextView? = null
    private var privateKeyEditText: EditText? = null
    private var receiveEditText: EditText? = null
    private var amountEditText: EditText? = null
    private var trc20EditText: EditText? = null
    private var transferBtn: Button? = null
    private var tronweb:TronWeb? = null
    private var mWebView: WebView? = null
    private var action: String = ""
    private var position: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fee_estimate_layout)
        setupContent()
        getData()
    }
    private fun setupContent(){
        trc20EditText  = findViewById(R.id.trc20Address)
        privateKeyEditText = findViewById(R.id.private_key)
        receiveEditText = findViewById(R.id.receive_address)
        amountEditText = findViewById(R.id.amount)
        title = findViewById(R.id.title)
        hashValue = findViewById(R.id.hashValue)
        transferBtn = findViewById(R.id.btn_transfer)
        mWebView = findViewById(R.id.webView)
        tronweb = TronWeb(this, _webView = mWebView!!)
        transferBtn?.setOnClickListener{
            preTransfer()
        }
    }
    private fun preTransfer(){
        val onCompleted = {result : Boolean,error:String ->
            if (result){
                println("onCompleted------->>>>>")
                println(result)
                getFeeEstimate()
            } else {
                println(error)
            }
        }
        val privateKey = privateKeyEditText?.text.toString()
        val node = if(position == 0) TRONMainNet else TRONNileNet
        if (tronweb?.isGenerateTronWebInstanceSuccess == false) {
            tronweb?.setup(true, privateKey, node = node,onCompleted = onCompleted)
        } else  {
            getFeeEstimate()
        }
    }
    private fun getFeeEstimate(){
        if (action == "estimateTRXTransferFee") {
            estimateTRXTransferFee()
        } else {
            estimateERC20TransferFee()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun estimateTRXTransferFee(){
        val toAddress = receiveEditText?.text.toString()
        val amount = amountEditText?.text.toString()
        val note = "Test data"
        val onCompleted = {state : Boolean, sendAccountResources:LinkedTreeMap<String, Any>,feeDic:LinkedTreeMap<String, Any>,error:String ->
            this.runOnUiThread {
                if (state){
                    println("estimateTRXTransferFee-----");
                    val activationFee = feeDic["activationFee"] as Double
                    val noteFee = feeDic["noteFee"] as Double
                    val requiredBandwidth = feeDic["requiredBandwidth"] as Double
                    val totalFee = activationFee + noteFee + requiredBandwidth / 1000
                    hashValue?.text =
                        "Resource Consumed  ${requiredBandwidth.toInt()} Bandwidth  \nFee    $totalFee TRX"

                } else {
                    hashValue?.text = error
                }
            }
        }
        tronweb?.estimateTRXTransferFee(toAddress, note, amount, onCompleted)
    }

    @SuppressLint("SetTextI18n")
    private fun estimateERC20TransferFee(){
        val toAddress = receiveEditText?.text.toString()
        val trc20ContractAddress = trc20EditText?.text.toString()
        val amount = amountEditText?.text.toString()
        val onCompleted = {state : Boolean, energyUsed:Double,energyFee:Double,error:String ->
            this.runOnUiThread {
                if (state){
                    val trxFee =  (energyUsed * energyFee) / 1_000_000
                    hashValue?.text =
                        "Resource Consumed  339 Bandwidth $energyUsed Energy\nFee    $trxFee TRX"
                } else {
                    hashValue?.text = error
                }
            }
        }
        val url = if(position == 0) TRONMainNet else TRONNileNet
        tronweb?.getFeeEstimate(
            url,
            toAddress,
            trc20ContractAddress,
            amount,
            onCompleted = onCompleted)
    }

    private fun getData() {
        //接收传值
        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            action = bundle.getString("action") ?: ""
            position = bundle.getInt("position") ?: 0
            println("this position:$position  action:$action")
            title?.text = if (position == 0) "主網轉帳手續費預估" else "Nile測試網轉帳手續費預估"
            val trc20ContractAddress =  if (position == 0) "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t" else "TXLAQ63Xg1NAzckPwKHvzw7CSEmLMEqcdj"
            trc20EditText?.setText(trc20ContractAddress)
            if (action == "estimateTRXTransferFee") {
                trc20EditText?.setVisibility(View.GONE)
            }
        }
    }
}