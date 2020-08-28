package com.github.rkbalgi

fun main(args: Array<String>) {

    val str = "abcd"

    val res = mutableListOf<String>()
    permute(str.toCharArray(), res, 0)

    res.forEachIndexed { i, s -> println("$i $s") }


}

fun permute(arr: CharArray, res: MutableList<String>, index: Int) {

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

