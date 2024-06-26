package com.james.tronwallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TestAdapter(arrayData: ArrayList<TestData>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listData: List<TestData> = arrayData
    var lambda = { position: Int ,action: String ->

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_test_layout,parent,false)
        return TestViewHolder1(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is TestViewHolder1) {
            holder.textView?.text = listData[position].testMessage
            holder.btnTest?.text = listData[position].btnTest
            holder.btnTest1?.text = listData[position].btnTest1
            holder.btnTest2?.text = listData[position].btnTest2
            holder.btnTest3?.text = listData[position].btnTest3
            holder.btnTest4?.text = listData[position].btnTest4
            holder.btnTest5?.text = listData[position].btnTest5
            holder.btnTest6?.text = listData[position].btnTest6
            holder.btnTest7?.text = listData[position].btnTest7
            holder.btnTest8?.text = listData[position].btnTest8
            holder.btnTest9?.text = listData[position].btnTest9
            holder.btnTest10?.text = listData[position].btnTest10
            holder.btnTest11?.text = listData[position].btnTest11
            holder.btnTest12?.text = listData[position].btnTest12


            holder.btnTest?.setOnClickListener{
                this.lambda(position,listData[position].btnTest)
            }
            holder.btnTest1?.setOnClickListener{
                this.lambda(position,listData[position].btnTest1)
            }
            holder.btnTest2?.setOnClickListener{
                this.lambda(position,listData[position].btnTest2)
            }
            holder.btnTest3?.setOnClickListener{
                this.lambda(position,listData[position].btnTest3)
            }
            holder.btnTest4?.setOnClickListener{
                this.lambda(position,listData[position].btnTest4)
            }
            holder.btnTest5?.setOnClickListener{
                this.lambda(position,listData[position].btnTest5)
            }
            holder.btnTest6?.setOnClickListener{
                this.lambda(position,listData[position].btnTest6)
            }
            holder.btnTest7?.setOnClickListener{
                this.lambda(position,listData[position].btnTest7)
            }
            holder.btnTest8?.setOnClickListener{
                this.lambda(position,listData[position].btnTest8)
            }
            holder.btnTest9?.setOnClickListener{
                this.lambda(position,listData[position].btnTest9)
            }
            holder.btnTest10?.setOnClickListener{
                this.lambda(position,listData[position].btnTest10)
            }
            holder.btnTest11?.setOnClickListener{
                this.lambda(position,listData[position].btnTest11)
            }
            holder.btnTest12?.setOnClickListener{
                this.lambda(position,listData[position].btnTest12)
            }
        }
    }

    override fun getItemCount(): Int {
        return listData.size
    }

}