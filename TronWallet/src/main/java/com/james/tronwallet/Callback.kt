package com.james.tronwallet

interface Callback {
    fun call(map: HashMap<String, Any>?)
}