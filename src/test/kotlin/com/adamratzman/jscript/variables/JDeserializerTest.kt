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
        println(deserializer.parseList("[\"test\", [3, 4.0]]").getJListFromParsedList().objects[1])
    }

    @Test
    fun parseObject() {
        println(deserializer.parseObject("""
          {
  object = {
    numbers = [4.3, 5, true],
    hello = "world"
  },
  list = [4.3, 5, false],
  azzerial = "is cool?"
}

        """.trimIndent()))
    }

}