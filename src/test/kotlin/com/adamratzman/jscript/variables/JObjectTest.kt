package com.adamratzman.jscript.variables

import org.junit.Test

internal class JObjectTest {
    @Test
    fun testToString() {
        val numObject = JList(mutableListOf(JDouble(4.3), JLong(5)))
        val nestedObject = JObject(mutableMapOf("numbers" to numObject, "hello" to "world"))
        val nestedObjectWithOtherJObject = JObject(mutableMapOf("object" to nestedObject, "list" to numObject, "azzerial" to "is cool?"))

        val complexObj = JObject(mutableMapOf("info" to JList(mutableListOf(numObject,nestedObject)), "user" to "test"))
        println(complexObj)

        val nestedList = JList(mutableListOf(nestedObject,nestedObject))
       // println(nestedList.toString())
    }
}