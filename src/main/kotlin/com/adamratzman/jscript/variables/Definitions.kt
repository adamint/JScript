package com.adamratzman.jscript.variables

open class JObject(var fields: MutableMap<String, Any>) {
    override fun toString(): String {
        val valueField = fields["value"]
        if (valueField is String || valueField is Number && fields.size == 1) {
            return if (valueField is String) "\"$valueField\""
            else valueField.toString()
        }

        val sb = StringBuilder()
        sb.append("{")
        var counter = 0
        fields.forEach { (name, value) ->
            sb.append("\n  ")
            if (name != "value") sb.append("$name = ")
            val serializedValue = when (value) {
                is JList -> value.toString()
                is JObject -> value.toString().split("\n").asSequence()
                        .mapIndexed { index, s -> (if (index > 0) "  " else "") + s}.joinToString("\n")
                is String -> "\"$value\""
                is Number -> value.toString()
                else -> throw IllegalArgumentException("Argument $value isn't of a recognizable type")
            }
            counter++
            sb.append(serializedValue)
            if (counter < fields.size) sb.append(",")
        }
        sb.append("\n}")
        return sb.toString()
    }
}

/**
 * Lists can contain anything, including other lists.
 */
class JList(fields: MutableList<JObject>) : JObject(mutableMapOf("value" to fields)) {
    override fun toString(): String {
        val sb = StringBuilder()
        fields.map { (_, jObject) -> jObject.toString() }.joinToString(",\n")
                .let { sb.append(it) }
        return sb.toString()
    }
}


/**
 * Parent number class. Accepted are 2 types: longs and doubles, nothing else
 */
open class JNumber(val backedValue: Number) : JObject(mutableMapOf("value" to backedValue))

class JLong(number: Long) : JNumber(number)
class JDouble(number: Double) : JNumber(number)

class JString(val string: String) : JObject(mutableMapOf("value" to string))

