package com.adamratzman.jscript.main

class Parser(val jscript: JScript) {
    fun parse(user: String, input: String) {
        // each statement will be terminated with a semi-colon (;) which will make parsing really easy
        val statements = input.splitToStatements()
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