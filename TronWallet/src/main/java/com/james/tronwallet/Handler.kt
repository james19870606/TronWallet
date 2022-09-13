package com.james.tronwallet

interface Handler {
    fun handler(map: HashMap<String, Any>?, callback: Callback)
}