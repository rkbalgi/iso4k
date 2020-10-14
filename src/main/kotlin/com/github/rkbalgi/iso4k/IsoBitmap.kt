package com.github.rkbalgi.iso4k

import com.google.common.primitives.Longs

class IsoBitmap(private val bmpData: ByteArray, var field: IsoField?, msg: Message?) {

    private var l1: Long = Longs.fromByteArray(bmpData.sliceArray(0..7))
    private var l2: Long = Longs.fromByteArray(bmpData.sliceArray(8..15))
    private var l3: Long = Longs.fromByteArray(bmpData.sliceArray(16..23))

    private var msg: Message? = null


    constructor() : this(ByteArray(24), null, null) {
    }

    fun isOn(pos: Int): Boolean {
        assert(pos in 1..192)

        return when {
            pos < 65 -> {
                l1.isBitOn(pos)
            }
            pos < 129 -> {
                l2.isBitOn(pos - 64)
            }
            else -> {
                l3.isBitOn(pos - 128)
            }
        }
    }

    fun setOn(pos: Int) {
        assert(pos in 1..192)
        val bp = when {
            pos < 65 -> {
                Triple(1, l1, 64 - pos)
            }
            pos < 129 -> {
                Triple(2, l2, 128 - pos)
            }
            else -> {
                Triple(3, l3, 192 - pos)
            }
        }

        var l: Long = 1
        l = l.shl(bp.third).or(bp.second)
        when (bp.first) {
            1 -> l1 = l
            2 -> {
                l2 = l
                setOn(1)
            }
            3 -> {
                l3 = l
                setOn(65)
            }
        }
    }

    /**
     * @return the bytes that make up the bitmap
     */
    fun bytes(): ByteArray {
        val bmpData = ByteArray(24)
        Longs.toByteArray(l1).copyInto(bmpData, destinationOffset = 0)
        Longs.toByteArray(l2).copyInto(bmpData, destinationOffset = 8)
        Longs.toByteArray(l3).copyInto(bmpData, destinationOffset = 16)

        return bmpData
    }

    fun get(fieldName: String): FieldData {

        val res = this.field!!.children?.filter { it.name == fieldName }?.stream()?.findFirst()
        if (res != null && res.isPresent && this.msg!!.fieldDataMap.containsKey(res.get())) {
            return this.msg!!.fieldDataMap[res.get()]!!
        }

        if (res == null || res.isEmpty) {
            throw NoSuchElementException("No field defined with name $fieldName")
        }
        throw FieldNotPresentException("No field data for field with name: $fieldName")


    }

    fun get(pos: Int): FieldData {

        println(msg?.fieldDataMap)

        val res = this.field!!.children?.filter { it.position == pos }?.stream()?.findFirst()
        if (res != null && res.isPresent && this.msg!!.fieldDataMap.containsKey(res.get())) {
            return this.msg!!.fieldDataMap[res.get()]!!
        }

        if (res == null || res.isEmpty) {
            throw NoSuchElementException("No field defined @ position $pos")
        }
        throw FieldNotPresentException("No field data @ position $pos")

    }


}

class FieldNotPresentException(msg: String) : Throwable(msg) {

}

fun Long.isBitOn(pos: Int): Boolean {
    return this.shr(64 - pos).and(0x01) == 0x01L
}
