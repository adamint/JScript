package com.adamratzman.jscript.variables

import org.junit.Test

internal class JObjectTest {
    @Test
    fun testToString() {
        val numObject = JList(mutableListOf(JDouble(4.3), JLong(5)))
        val nestedObject = JObject(mutableMapOf("numbers" to numObject, "hello" to "world"))
        val nestedObjectWithOtherJObject = JObject(mutableMapOf("object" to nestedObject, "list" to numObject, "azzerial" to "is cool?"))
        println(nestedObjectWithOtherJObject.toString())
    }
}