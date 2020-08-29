package com.github.rkbalgi.iso4k

import com.github.rkbalgi.iso4k.charsets.Charsets
import com.sun.org.apache.xpath.internal.operations.Bool

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Byte.parseByte
import java.lang.UnsupportedOperationException
import java.nio.ByteBuffer
import java.sql.Types.BINARY
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
    val dataEncoding: DataEncoding,
    val lengthEncoding: DataEncoding?,
    val children: Array<IsoField>?
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

data class FieldData(val field: IsoField, val data: ByteArray)

val LOG: Logger = LoggerFactory.getLogger(IsoField::class.java)

fun IsoField.parse(buf: ByteBuffer): FieldData {

    when (type) {
        FieldType.Fixed -> {
            parseFixed(this, buf)
        }
        FieldType.Variable -> {
            parseVariable(this, buf)
        }
        FieldType.Bitmapped -> {
            parseBitmapped(this, buf)
        }
    }


    return FieldData(this, ByteArray(10))
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

fun parseBitmapped(field: IsoField, buf: ByteBuffer) {

    when (field.dataEncoding) {
        DataEncoding.BINARY -> {

            val bmpData = ByteArray(24);
            buf.get(bmpData, 0, 8)
            if (bmpData[0].isHighBitSet()) {
                //secondary bitmap present
                buf.get(bmpData, 8, 8)
                if (bmpData[8].isHighBitSet()) {
                    //tertiary also present
                    buf.get(bmpData, 16, 8)
                }
            }
        }
        else -> {
            TODO("bitmap unimplemented for encoding type: $field.dataEncoding")
        }
    }
}

fun parseFixed(field: IsoField, buf: ByteBuffer) {

    buf.mark()
    val tmp = ByteArray(field.len)
    buf.get(tmp)
    LOG.debug("field {}: data: {}", field.name, tmp.decodeToHexString())
    buf.reset()

    if (field.hasChildren()) {
        field.children!!.forEach {
            it.parse(buf)
        }
    }

}


fun parseVariable(field: IsoField, buf: ByteBuffer) {


    val len = readFieldLength(field, buf)
    val tmp = ByteArray(len)
    buf.get(tmp)
    LOG.debug("field {}: data: {}", field.name, tmp.decodeToHexString())

    val newBuf = ByteBuffer.wrap(tmp)

    if (field.hasChildren()) {
        field.children!!.forEach {
            it.parse(newBuf)
        }
    }

}

internal fun readFieldLength(field: IsoField, buffer: ByteBuffer): Int {
    val tmp = ByteArray(field.len)
    buffer.get(tmp)
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
