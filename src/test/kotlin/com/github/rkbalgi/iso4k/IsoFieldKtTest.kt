package com.github.rkbalgi.iso4k

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class IsoFieldKtTest {

    @Test
    @DisplayName("build ASCII length indicator")
    fun `build ASCII length indicator`() {

        var res = buildLengthIndicator(DataEncoding.ASCII, 2, 18)
        assertArrayEquals(arrayOf<Byte>(0x31, 0x38).toByteArray(), res)

        res = buildLengthIndicator(DataEncoding.ASCII, 3, 18)
        assertArrayEquals(arrayOf<Byte>(0x30, 0x31, 0x38).toByteArray(), res)

        res = buildLengthIndicator(DataEncoding.ASCII, 3, 118)
        assertArrayEquals(arrayOf<Byte>(0x31, 0x31, 0x38).toByteArray(), res)
        
        res = buildLengthIndicator(DataEncoding.ASCII, 3, 1)
        assertArrayEquals(arrayOf<Byte>(0x30, 0x30, 0x31).toByteArray(), res)


    }
}