package com.github.rkbalgi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.rkbalgi.iso4k.Spec
import com.github.rkbalgi.iso4k.fromHexString
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


internal class MiscTests {


    @Test
    fun yamlTest() {

        val spec = Spec.spec("SampleSpec")

        val msgData =
            fromHexString("31313030f02420000000100e000000010000000131363435363739303938343536373132333530303430303030303030303030303030323937373935383132323034f8f4f077fcbd9ffc0dfa6f001072657365727665645f310a9985a28599a5858460f2f0f1f1383738373736323235323531323334e47006f5de8c70b9")
        val msg = spec?.message("1100 - Authorization")?.parse(msgData)

        assertNotNull(msg)
        assertEquals("1100", msg.get("message_type").encodeToString())
        assertEquals("004000", msg.bitmap().get(3).encodeToString())
        assertEquals("reserved_1", msg.bitmap().get(61).encodeToString())


    }

    @Test
    fun testPermute() {

        val str = "abcd"

        val res = mutableListOf<String>()
        permute(str.toCharArray(), res, 0)

        res.forEachIndexed { i, s -> println("$i $s") }


    }

    private fun permute(arr: CharArray, res: MutableList<String>, index: Int) {

        if (index == arr.size - 1) {
            res.add(String(arr))
            return
        }


        for (j in index until arr.size) {

            //do swap
            val t = arr[index]
            arr[index] = arr[j]
            arr[j] = t

            permute(String(arr).toCharArray(), res, index + 1);

            val t2 = arr[index]
            arr[index] = arr[j]
            arr[j] = t2


        }

    }

}

