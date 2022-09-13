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
        listData.add(TestData("MAIN","trxTransfer","trc20Transfer","getTRC20TokenBalance","getTRXBalance","getAccount","resetTronWebPrivateKey"))
        listData.add(TestData("NILE","trxTransfer","trc20Transfer","getTRC20TokenBalance","getTRXBalance","getAccount","resetTronWebPrivateKey"))
        return listData
    }
    private fun setSimpleAdapter(listData: ArrayList<TestData>){
        val testAdapter = TestAdapter(listData)
        testAdapter.lambda = { position: Int ,action: String ->
            var intent: Intent?  = null
            intent = when (action) {
                "trxTransfer","trc20Transfer" -> {
                    Intent(this@MainActivity, TransferActivity::class.java)
                }
                "getTRC20TokenBalance","getTRXBalance" -> {
                    Intent(this@MainActivity, GetBalanceActivity::class.java)
                }
                "getAccount" -> {
                    Intent(this@MainActivity, GetAccountActivity::class.java)
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