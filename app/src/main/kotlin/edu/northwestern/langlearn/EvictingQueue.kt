package edu.northwestern.langlearn

import android.support.v4.util.CircularArray

class EvictingQueue<T>(val max: Int) {
    private val array: CircularArray<T> = CircularArray(max)

    override fun toString(): String {
        var s: String = "{${ get(0) }"

        for (i in 1..(size() - 1)) {
            s = "$s,${ get(i) }"
        }

        return "$s}"
    }

    fun add(elem: T): Unit {
        if (size() == max) pop()

        array.addFirst(elem)
    }

    fun pop(): T = array.popLast()
    fun get(n: Int): T = array.get(size() - n - 1)
    fun size(): Int = array.size()
}
