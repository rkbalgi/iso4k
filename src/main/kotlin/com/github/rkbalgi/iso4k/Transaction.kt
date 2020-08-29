package com.github.rkbalgi.iso4k

class Transaction {


    private lateinit var request: Message;
    private lateinit var response: Message;

    constructor(_req: Message){
        request=_req
    }

    fun request(): Message {
        return request
    }

    fun response(): Message {
        return response
    }
}