package io.github.rkbalgi

import io.github.rkbalgi.iso4k.Spec
import io.github.rkbalgi.iso4k.fromHexString
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


internal class MiscTests {


    @Test
    fun findMessageTest() {

        val spec = Spec.spec("SampleSpec")

        val msgData = fromHexString("31313030")
        Assert.assertEquals("1100/1110 - Authorization", spec!!.findMessage(msgData))
        Assert.assertNull(spec.findMessage(fromHexString("31313131")))

    }

    @Test
    fun yamlTest() {

        val spec = Spec.spec("SampleSpec")

        val msgData =
            fromHexString("31313030f02420000000100e000000010000000131363435363739303938343536373132333530303430303030303030303030303030323937373935383132323034f8f4f077fcbd9ffc0dfa6f001072657365727665645f310a9985a28599a5858460f2f0f1f1383738373736323235323531323334e47006f5de8c70b9")
        val msg = spec?.message("1100/1110 - Authorization")?.parse(msgData)


        assertNotNull(msg)
        assertEquals("1100", msg.get("message_type")?.encodeToString())
        assertEquals("004000", msg.bitmap().get(3).encodeToString())
        assertEquals("reserved_1", msg.bitmap().get(61).encodeToString())


    }

    @Test
    fun test__mti_checks() {
        val spec = Spec.spec("SampleSpec")!!

        assertTrue { spec.isRequest("1100") }
        assertTrue { spec.isResponse("1110") }
        assertTrue { spec.responseMTI("1420") == "1430" }
    }

}

