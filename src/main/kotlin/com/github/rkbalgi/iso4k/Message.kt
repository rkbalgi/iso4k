package com.github.rkbalgi.iso4k

class Message(val messageSegment: MessageSegment) {

    private val fieldDataMap: Map<String, FieldData> = mutableMapOf();
    private var bitmap:IsoBitmap=IsoBitmap(ByteArray(24))


}