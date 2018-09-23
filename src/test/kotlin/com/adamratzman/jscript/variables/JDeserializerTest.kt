package com.adamratzman.jscript.variables

import org.junit.Test

internal class JDeserializerTest {
    val deserializer = JDeserializer(true)
    @Test
    fun fromString() {
    }

    @Test
    fun parseList() {
        println(deserializer.parseList("[3.0, 5, \"hello world\" ]").getJListFromParsedList().objects[1])
    }
}