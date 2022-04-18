package io.github.rkbalgi.iso4k

import com.fasterxml.jackson.annotation.JsonProperty
import java.nio.ByteBuffer

/**
 * A MessageSegment defines the layout of structure of a message (a request or a response etc)
 *
 */
class MessageSegment(val name: String) {


    @JsonProperty("id")
    val id: Int = 0
    private lateinit var spec: Spec

    @JsonProperty("selector")
    private lateinit var selectors: List<String>

    @JsonProperty("fields")
    private lateinit var fields: List<IsoField>


    fun spec(): Spec {
        return spec
    }

    fun fields(): List<IsoField> {
        return fields;
    }

    fun selectorMatch(selector: String): Boolean {
        return selectors.stream().filter { it.equals(selector) }.findAny().isPresent
    }

    fun parse(msgData: ByteArray): Message {
        val msg = Message(this)
        val msgBuf = ByteBuffer.wrap(msgData)
        fields.forEach { it.parse(msg, msgBuf) }
        return msg
    }

}
