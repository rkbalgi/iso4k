package com.github.rkbalgi.iso4k


import com.fasterxml.jackson.annotation.JsonProperty
import com.github.rkbalgi.iso4k.charsets.Charsets
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.util.*
import kotlin.experimental.and

enum class DataEncoding {
    ASCII, BCD, BINARY, EBCDIC
}

enum class FieldType {
    Fixed, Variable, Terminated, Bitmapped
}

data class IsoField(
    val id: Int,
    val name: String,
    val type: FieldType,
    val len: Int,
    @JsonProperty("data_encoding") val dataEncoding: DataEncoding,
    @JsonProperty("len_encoding") val lengthEncoding: DataEncoding?,
    val children: Array<IsoField>?,
    var position: Int = 0,
    var key: Boolean = false,

    ) {


    companion object {
        val LOG: Logger = LoggerFactory.getLogger(IsoField::class.java)
    }

    fun hasChildren(): Boolean {
        return children != null && children.isNotEmpty()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        } else {
            if (other is IsoField) {
                return this.id == other.id && this.name == other.name
            }
        }

        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(id, name)
    }


}

data class FieldData(val field: IsoField, val data: ByteArray) {
    fun encodeToString(): String {
        return Charsets.toString(data, field.dataEncoding)
    }
}

val LOG: Logger = LoggerFactory.getLogger(IsoField::class.java)

fun IsoField.parse(msg: Message, buf: ByteBuffer) {

    when (type) {
        FieldType.Fixed -> parseFixed(this, msg, buf)
        FieldType.Variable -> parseVariable(this, msg, buf)
        FieldType.Bitmapped -> parseBitmapped(this, msg, buf)
        FieldType.Terminated -> TODO("support terminated fields")
    }

}

fun IsoField.parse(buf: ByteBuffer): String {

    when (type) {
        FieldType.Fixed -> {

            val data = ByteArray(len)
            buf.get(data)
            return FieldData(this, data).encodeToString()
        }


        else -> TODO("only fixed type supported for header fields")
    }

}

fun ByteArray.decodeToHexString(): String {
    return this.joinToString("") { String.format("%02x", (0xff and it.toInt())) }
}

/**
 * @param str A string containing hex characters
 * @return ByteArray
 */
fun fromHexString(str: String): ByteArray {
    assert(str.length % 2 == 0)
    val res = ByteArray(str.length / 2)

    var i = 0
    str.chunked(2).forEach { a ->
        res[i++] = Integer.parseInt(a, 16).toByte()
    }
    return res
}

fun Byte.isHighBitSet(): Boolean {
    return (this and 0x80.toByte()) == 0x80.toByte()
}

private fun parseBitmapped(field: IsoField, msg: Message, buf: ByteBuffer) {

    when (field.dataEncoding) {
        DataEncoding.BINARY -> {

            val bmpData = ByteArray(24);



            buf.get(bmpData, 0, 8)
            //buf.position(buf.position() + 8)
            if (bmpData[0].isHighBitSet()) {
                //secondary bitmap present
                buf.get(bmpData, 8, 8)
                //buf.position(buf.position() + 8)
                if (bmpData[8].isHighBitSet()) {
                    //tertiary also present
                    buf.get(bmpData, 16, 8)
                    //buf.position(buf.position() + 8)
                }
            }
            msg.setBitmap(IsoBitmap(bmpData, field, msg))
            field.children?.filter { it.position > 0 && msg.bitmap().isOn(it.position) }?.forEach { it.parse(msg, buf) }
        }
        else -> {
            TODO("bitmap unimplemented for encoding type: $field.dataEncoding")
        }
    }
}

private fun parseFixed(field: IsoField, msg: Message, buf: ByteBuffer) {


    val data = ByteArray(field.len)
    buf.get(data)
    val fieldData = FieldData(field, data)
    setAndLog(msg, fieldData)


    if (field.hasChildren()) {
        field.children?.forEach {
            //TODO:: can rewind and try without a fresh allocation
            val newBuf = ByteBuffer.wrap(data)
            it.parse(msg, newBuf)
        }
    }

}

internal fun setAndLog(msg: Message, fieldData: FieldData) {
    msg.setFieldData(fieldData.field, fieldData)
    LOG.debug(
        "field ${fieldData.field.name}: data(raw): ${fieldData.data.decodeToHexString()} data(encoded): ${fieldData.encodeToString()}"
    )

}


fun parseVariable(field: IsoField, msg: Message, buf: ByteBuffer) {


    val len = readFieldLength(field, buf)
    val data = ByteArray(len)

    buf.get(data)
    val fieldData = FieldData(field, data)
    setAndLog(msg, fieldData)


    if (field.hasChildren()) {
        val newBuf = ByteBuffer.wrap(data)
        field.children?.forEach {
            it.parse(msg, newBuf)
        }
    }

}

internal fun readFieldLength(field: IsoField, buffer: ByteBuffer): Int {
    val tmp = ByteArray(field.len)
    buffer.get(tmp)

    if (field.lengthEncoding == DataEncoding.BINARY) {
        return Charsets.toString(tmp, field.lengthEncoding).toInt(16)
    }

    return Charsets.toString(tmp, field.lengthEncoding!!).toInt()

}

internal fun buildLengthIndicator(encoding: DataEncoding, len: Int, dataLength: Int): ByteArray {

    return when (encoding) {
        DataEncoding.BCD -> {
            val tmp = String.format("%0${len * 2}d", dataLength)
            Charsets.fromString(tmp, encoding)
        }
        DataEncoding.BINARY -> {
            val tmp = String.format("%0${len * 2}x", dataLength)
            Charsets.fromString(tmp, encoding)
        }
        DataEncoding.ASCII, DataEncoding.EBCDIC -> {
            val formatSpecifier = "%0${len}d";
            val tmp = String.format(formatSpecifier, dataLength)
            Charsets.fromString(tmp, encoding)
        }
    }
}
