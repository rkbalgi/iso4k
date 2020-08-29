package com.github.rkbalgi.iso4k

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class IsoFieldKtTest {

    @Test
    @DisplayName("build ASCII length indicator")
    fun testBuildAsciiLengthIndicator() {

        var res = buildLengthIndicator(DataEncoding.ASCII, 2, 18)
        assertArrayEquals(arrayOf<Byte>(0x31, 0x38).toByteArray(), res)

        res = buildLengthIndicator(DataEncoding.ASCII, 3, 18)
        assertArrayEquals(arrayOf<Byte>(0x30, 0x31, 0x38).toByteArray(), res)

        res = buildLengthIndicator(DataEncoding.ASCII, 3, 118)
        assertArrayEquals(arrayOf<Byte>(0x31, 0x31, 0x38).toByteArray(), res)

        res = buildLengthIndicator(DataEncoding.ASCII, 3, 1)
        assertArrayEquals(arrayOf<Byte>(0x30, 0x30, 0x31).toByteArray(), res)


    }


    @Test
    @DisplayName("build EBCDIC length indicator")
    fun testBuildEbcdicLengthIndicator() {


        var res = buildLengthIndicator(DataEncoding.EBCDIC, 2, 18)
        assertArrayEquals(fromHexString("f1f8"), res)

        res = buildLengthIndicator(DataEncoding.EBCDIC, 3, 18)
        assertArrayEquals(fromHexString("f0f1f8"), res)

        res = buildLengthIndicator(DataEncoding.EBCDIC, 3, 118)
        assertArrayEquals(fromHexString("f1f1f8"), res)

        res = buildLengthIndicator(DataEncoding.EBCDIC, 3, 1)
        assertArrayEquals(fromHexString("f0f0f1"), res)


    }

    @Test
    @DisplayName("build Binary length indicator")
    fun testBuildBinaryLengthIndicator() {


        var res = buildLengthIndicator(DataEncoding.BINARY, 2, 18)
        assertArrayEquals(fromHexString("0012"), res)

        res = buildLengthIndicator(DataEncoding.BINARY, 1, 18)
        assertArrayEquals(fromHexString("12"), res)


    }

    @Test
    @DisplayName("build BCD length indicator")
    fun testBuildBcdLengthIndicator() {


        var res = buildLengthIndicator(DataEncoding.BCD, 2, 18)
        assertArrayEquals(fromHexString("0018"), res)

        res = buildLengthIndicator(DataEncoding.BCD, 1, 18)
        assertArrayEquals(fromHexString("18"), res)


    }
}