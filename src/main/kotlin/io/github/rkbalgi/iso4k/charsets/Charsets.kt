package io.github.rkbalgi.iso4k.charsets;

import io.github.rkbalgi.iso4k.DataEncoding
import io.github.rkbalgi.iso4k.decodeToHexString
import io.github.rkbalgi.iso4k.fromHexString
import java.nio.charset.Charset

class Charsets {

    companion object {

        fun toString(data: ByteArray, encoding: DataEncoding): String {
            return when (encoding) {
                DataEncoding.ASCII -> {
                    String(data)
                }
                DataEncoding.EBCDIC -> {
                    String(data, Charset.forName("cp037"))
                }
                DataEncoding.BCD, DataEncoding.BINARY -> {
                    data.decodeToHexString()
                }
            }
        }

        fun fromString(data: String, encoding: DataEncoding): ByteArray {
            return when (encoding) {
                DataEncoding.ASCII -> {
                    data.toByteArray(Charset.forName("US-ASCII"))
                }
                DataEncoding.EBCDIC -> {
                    data.toByteArray(Charset.forName("cp037"))
                }
                DataEncoding.BCD, DataEncoding.BINARY -> {
                    fromHexString(data)
                }
            }
        }
    }


}