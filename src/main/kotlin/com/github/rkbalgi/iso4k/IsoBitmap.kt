package com.github.rkbalgi.iso4k

class IsoBitmap(bmpData: ByteArray) {

    fun isOn(pos:Int): Boolean{
        assert(pos in 2..192)

        return true
    }

    fun setOn(pos: Int){
        assert(pos in 2..192)


    }

}
