package com.github.rkbalgi.iso4k

/**
 * A MessageSegment defines the layout of structure of a message (a request or a response etc)
 *
 */
class MessageSegment() {

    private lateinit var spec: Spec
    private lateinit var selectors: List<String>
    private lateinit var fields: List<IsoField>


    constructor(_spec: Spec) {
        spec = _spec
    }

    fun spec(): Spec {
        return spec
    }

    fun fields(): List<IsoField> {
        return fields;
    }

    fun selectorMatch(selector: String) :Boolean{
        return false;
    }

}
