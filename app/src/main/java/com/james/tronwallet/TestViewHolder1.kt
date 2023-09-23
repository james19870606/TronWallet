package com.james.tronwallet

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TestViewHolder1(itemView: View): RecyclerView.ViewHolder(itemView) {
    var textView: TextView? = null
    var btnTest: Button? = null
    var btnTest1: Button? = null
    var btnTest2: Button? = null
    var btnTest3: Button? = null
    var btnTest4: Button? = null
    var btnTest5: Button? = null
    var btnTest6: Button? = null
    var btnTest7: Button? = null
    var btnTest8: Button? = null

    init {
        textView = itemView.findViewById(R.id.text_item_test)
        btnTest = itemView.findViewById(R.id.btn_item_test)
        btnTest1 = itemView.findViewById(R.id.btn_item_test1)
        btnTest2 = itemView.findViewById(R.id.btn_item_test2)
        btnTest3 = itemView.findViewById(R.id.btn_item_test3)
        btnTest4 = itemView.findViewById(R.id.btn_item_test4)
        btnTest5 = itemView.findViewById(R.id.btn_item_test5)
        btnTest6 = itemView.findViewById(R.id.btn_item_test6)
        btnTest7 = itemView.findViewById(R.id.btn_item_test7)
        btnTest8 = itemView.findViewById(R.id.btn_item_test8)

    }
}