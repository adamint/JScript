package com.adamratzman.jscript.main

import com.adamratzman.jscript.variables.JObject

class Parser(val jscript: JScript, val integratedObjects: MutableList<Class<out JObject>>) {
    fun parse(user: String, input: String) {
        val localVariables = mutableMapOf<String,JObject>()
        // each statement will be terminated with a semi-colon (;) which will make parsing really easy
        val statements = input.splitToStatements().map { it.trim() }
        statements.forEach { statement ->
            if (statement.isNotEmpty()) {
                /*
                there must be at least one argument which *must be* either a variable name
                OR START WITH the name of a function. Otherwise, this statement is INVALID
                 */

                // case 1: statement starts with variable (MUST BE LOCAL!)
                /*
                Available actions:

                variable = object
                if variable is not an assignment and resolves to null, an exception is thrown
                IF VARIABLE EXISTS ALREADY:
                if JNumber:
                variable++ translated to
                variable--
                variable += other
                variable -= other

                if JString:
                variable += other
                variabl
                Any other found string is invalid
                 */
                val potentialVariableEnd = if (statement.indexOf(' ') != -1) statement.indexOf(' ')
                else if (statement.indexOf('+') != -1) statement.indexOf('+')
                else if (statement.indexOf('-') != -1) statement.indexOf('-')
                else -1

                // this involves manipulation of a variable
                if (potentialVariableEnd != -1) {
                    val variable = localVariables
                }
            }
        }
    }
}


/**
 * Split a [String] code input into statements that will then be procedurally executed
 */
fun String.splitToStatements(): List<String> {
    val statementEndPositions = mutableListOf<Int>()
    /*
     Valid EOS semicolons must NOT be:
     1) in a string
     2) escaped with a backslash
     */
    val quotePositions = getStringPositions()
    forEachIndexed { i, char ->
        if (char == ';') {
            var valid = true

            if (i > 0 && this[i - 1] == '\\') valid = false // check escaped
            for ((start, end) in quotePositions) {
                if (i >= start && (end == -1 || i <= end)) valid = false // check in a string
            }

            if (valid) statementEndPositions.add(i)
        }
    }

    return statementEndPositions.mapIndexed { i, position ->
        if (i == 0) substring(0, position)
        else {
            val start = if (this[statementEndPositions[i - 1] + 1] == '\n') statementEndPositions[i - 1] + 2
            else statementEndPositions[i - 1] + 1
            substring(start, position).trim()
        }
    }
}

/**
 * Match patterns of "" which must NOT be:
 * 1) escaped with a backslash (\)
 */
internal fun String.getStringPositions(): List<Pair<Int, Int>> {
    val quotationMarkPositions = mutableListOf<Int>()

    forEachIndexed { i, char ->
        if (char == '"') {
            if (i == 0 || i == lastIndex) quotationMarkPositions.add(i)
            else if (this[i - 1] != '\\') quotationMarkPositions.add(i)
        }
    }

    return quotationMarkPositions.asSequence().chunked(2).map { group -> group[0] to group.getOrElse(1) { -1 } }.toList()
}