package com.adamratzman.jscript.main

import com.adamratzman.jscript.implementations.JScriptInMemoryImplementation
import org.junit.Test

internal class ParserTest {
    val jScript = JScriptInMemoryImplementation()
    @Test
    fun parse() {
    }

    @Test
    fun splitToStatements() {
        println("""\"""[0] == '\\')
        val testStrings  = listOf("""
            this is a; test;
            statement three goes here;
            this isn't the end\; of the statement\; but this is;
        """.trimIndent())
        testStrings.forEach { println(it.splitToStatements().joinToString("\n")) }
    }

    @Test
    fun `getStringPositions$JSimpleScript_main`() {
        val testStrings = listOf("""test"test" q"\"q """.trimIndent())
        testStrings.forEach { println(it.getStringPositions()) }
    }
}