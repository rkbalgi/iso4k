package io.github.rkbalgi.iso4k

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.common.base.Strings
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.ByteBuffer
import kotlin.io.path.Path
import kotlin.io.path.readText


private const val specLocationProperty = "io.github.rkbalgi.iso4k.specsLocation"

data class MTIPair(
    @JsonProperty("request_mti") val requestMTI: String,
    @JsonProperty("response_mti") val responseMTI: String
)

public class Spec(val name: String, val id: Int) {

    private val req2responseMap = mutableMapOf<String, String>()

    @JsonProperty("request_response_mti_mapping")
    private var requestResponseMTIMapping: List<MTIPair> = emptyList()

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

    fun findMessage(msgData: ByteArray): String? {
        val bb = ByteBuffer.wrap(msgData)
        var headerVal = headerFields.stream().map { it.parse(bb) }.reduce { acc, a -> (acc + a) }.get()


        var msgSegment = messageSegments.stream().filter { it.selectorMatch(headerVal) }.findFirst()
        return if (msgSegment.isPresent)
            msgSegment.get().name
        else
            null


    }

    private fun init() {
        requestResponseMTIMapping.stream().forEach {
            req2responseMap[it.requestMTI] = it.responseMTI
        }
        messageSegments.stream().forEach{it.spec()}
    }

    fun isRequest(mti: String): Boolean = req2responseMap.containsKey(mti)


    fun isResponse(mti: String): Boolean =
        req2responseMap.values.stream().filter { mti == it }.findFirst().isPresent


    fun responseMTI(mti: String): String? {
        var response = req2responseMap.keys.stream().filter { it == mti }.findFirst()

        return if (response.isPresent) {
            req2responseMap[response.get()]
        } else {
            null
        }
    }

    companion object {


        private val LOG = LoggerFactory.getLogger(javaClass)

        @Volatile
        private var initialized: Boolean = false
        private var specMap = mutableMapOf<String, Spec>()


        private fun loadSpecs() {

            val objectMapper = ObjectMapper(YAMLFactory())
            objectMapper.registerKotlinModule()

            val specLocation = System.getProperty(specLocationProperty)
            if (!Strings.isNullOrEmpty(specLocation)) {
                LOG.info("Loading spec definitions from $specLocation")

                val specDir = File(specLocation)

                if (specDir.exists() && specDir.isDirectory) {

                    val fileContent = Path(specLocation).resolve("specs.yml").readText(Charsets.UTF_8)
                    val allSpecs = objectMapper.readValue<List<String>>(fileContent)

                    allSpecs.forEach {
                        val fileContent = Path(specLocation).resolve(it).readText(Charsets.UTF_8)
                        val spec = objectMapper.readValue<Spec>(fileContent)
                        specMap[spec.name] = spec
                    }

                } else {
                    throw java.lang.RuntimeException("$specLocationProperty doesn't point to a valid directory")
                }

            } else {
                //try to read from classpath
                LOG.info("Loading spec definitions from classpath")
                val allSpecs = objectMapper.readValue<List<String>>(Spec::javaClass.javaClass.getResource("/specs.yml"))
                allSpecs.forEach {
                    val spec = objectMapper.readValue<Spec>(Spec::javaClass.javaClass.getResource(it))
                    specMap[spec.name] = spec
                }

            }

            specMap.values.stream().forEach { it.init() }
            initialized = true
        }

        fun allSpecs(): List<Spec> {
            var listOfAllSpecs = mutableListOf<Spec>().apply {
                specMap.values.stream().forEach { this.add(it) }

            }
            return listOfAllSpecs

        }

        /**
         * spec returns a Spec given its name
         */
        fun spec(name: String): Spec? {

            if (!initialized) {
                loadSpecs()
            }

            if (initialized) {
                return specMap[name]
            }
            throw RuntimeException("iso4k not initialized")

        }
    }


}
