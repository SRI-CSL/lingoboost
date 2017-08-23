package edu.northwestern.langlearn

import android.support.v4.util.CircularArray

class EvictingQueue<T>(val max: Int) {
    private val array: CircularArray<T> = CircularArray(max)

    // = (1..(size() - 1)).fold("{${ get(0) }") { acc, i -> "$acc, ${ get(i) }" }.plus("}")
    override fun toString(): String = (0..(size() - 1)).joinToString(prefix = "{", postfix = "}") { "${ get(it) }" }

    fun add(elem: T): Unit {
        if (size() == max) pop()

        array.addFirst(elem)
    }

    fun pop(): T = array.popLast()
    fun get(n: Int): T = array.get(size() - n - 1)
    fun size(): Int = array.size()
}
