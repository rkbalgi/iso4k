package com.github.rkbalgi.iso4k

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule



class Spec(val name: String, val id: Int) {


    @JsonProperty("header_fields")
    private var headerFields: List<IsoField> = emptyList()

    @JsonProperty("messages")
    private var messageSegments: List<MessageSegment> = emptyList()


    constructor(name: String, headerSegment: List<IsoField>, messageSegments: List<MessageSegment>) : this(name, 0) {

        this.headerFields = headerSegment
        this.messageSegments = messageSegments
    }


    fun header(): List<IsoField> {
        return headerFields
    }

    fun messages(): List<MessageSegment> {
        return messageSegments
    }

    fun message(msgName: String): MessageSegment? {
        return messageSegments.find { it.name == msgName }
    }

    companion object {

        @Volatile
        private var initialized: Boolean = false
        private var specMap = mutableMapOf<String, Spec>()

        /**
         * spec returns a Spec given its name
         */
        fun spec(name: String): Spec? {

            if (initialized) {
                return specMap[name]
            }

            val objectMapper = ObjectMapper(YAMLFactory())
            objectMapper.registerKotlinModule()


            val allSpecs = objectMapper.readValue<List<String>>(Spec::javaClass.javaClass.getResource("/specs.yml"))
            allSpecs.forEach {
                val spec = objectMapper.readValue<Spec>(Spec::javaClass.javaClass.getResource(it))
                specMap[spec.name] = spec
            }

            initialized = true
            return specMap[name]
        }
    }


}
