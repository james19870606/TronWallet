package com.james.tronwallet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private var listView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView = findViewById(R.id.list_main)
        val listData = initData()
        setSimpleAdapter(listData)
    }

    private fun initData(): ArrayList<TestData> {
        val listData = ArrayList<TestData>()
        val mainData = TestData(
            "MAIN",
            "trxTransfer",
            "trc20Transfer",
            "getTRC20TokenBalance",
            "getTRXBalance",
            "getAccount",
            "resetTronWebPrivateKey",
            "createRandom",
            "CreateAccount",
            "ImportAccountFromMnemonic",
            "getAccountResource",
            "getChainParameters",
            "estimateTRC20TransferFee",
            "estimateTRXTransferFee",
            "ImportAccountFromPrivateKey")
        val nileData = TestData(
            "Nile",
            "trxTransfer",
            "trc20Transfer",
            "getTRC20TokenBalance",
            "getTRXBalance",
            "getAccount",
            "resetTronWebPrivateKey",
            "createRandom",
            "CreateAccount",
            "ImportAccountFromMnemonic",
            "getAccountResource",
            "getChainParameters",
            "estimateTRC20TransferFee",
            "estimateTRXTransferFee",
            "ImportAccountFromPrivateKey")
        listData.add(mainData)
        listData.add(nileData)
        return listData
    }
    private fun setSimpleAdapter(listData: ArrayList<TestData>){
        val testAdapter = TestAdapter(listData)
        testAdapter.lambda = { position: Int ,action: String ->
            var intent: Intent?  = null
            intent = when (action) {
                "createRandom" -> {
                    Intent(this@MainActivity, CreateRandomActivity::class.java)
                }
                "CreateAccount" -> {
                    Intent(this@MainActivity, CreateAccountActivity::class.java)
                }
                "ImportAccountFromMnemonic" -> {
                    Intent(this@MainActivity, ImportAccountFromMnemonicActivity::class.java)
                }
                "trxTransfer","trc20Transfer" -> {
                    Intent(this@MainActivity, TransferActivity::class.java)
                }
                "getTRC20TokenBalance","getTRXBalance" -> {
                    Intent(this@MainActivity, GetBalanceActivity::class.java)
                }
                "getAccount" -> {
                    Intent(this@MainActivity, GetAccountActivity::class.java)
                }
                "getAccountResource" -> {
                    Intent(this@MainActivity, GetAccountResourceActivity::class.java)
                }
                "getChainParameters" -> {
                    Intent(this@MainActivity, GetChainParametersActivity::class.java)
                }
                "estimateTRC20TransferFee" -> {
                    Intent(this@MainActivity, FeeEstimateActivity::class.java)
                }
                "estimateTRXTransferFee" -> {
                    Intent(this@MainActivity, FeeEstimateActivity::class.java)
                }
                "ImportAccountFromPrivateKey" -> {
                    Intent(this@MainActivity, ImportAccountFromPrivateKeyActivity::class.java)
                }
                else -> {
                    Intent(this@MainActivity, ResetTronWebPrivateKeyActivity::class.java)
                }
            }
            intent.putExtra("position", position)
            intent.putExtra("action", action)
            startActivity(intent)
        }
        listView?.adapter = testAdapter
        //LayoutManager必须设置,否则不显示列表
        listView?.layoutManager = LinearLayoutManager(this)
    }

}