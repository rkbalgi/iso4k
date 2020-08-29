package com.github.rkbalgi.iso4k

class Spec() {

    private lateinit var headerSegment: MessageSegment
    private lateinit var messageSegments: List<MessageSegment>

    constructor(headerSegment: MessageSegment, messageSegments: List<MessageSegment>) {
        this.headerSegment = headerSegment
        this.messageSegments = messageSegments
    }


    fun header(): MessageSegment {
        return headerSegment
    }

    fun messages(): List<MessageSegment> {
        return messageSegments
    }


}
