package com.github.rkbalgi.iso4k

class Transaction(private val req: Message) {


    private lateinit var response: Message;

    fun request(): Message {
        return req
    }

    fun response(): Message {
        return response
    }
}