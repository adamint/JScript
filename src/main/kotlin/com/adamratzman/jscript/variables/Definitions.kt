package com.adamratzman.jscript.variables



open class JObject(var fields: MutableMap<String, Any>) {
    override fun toString(): String {
        val valueField = fields["value"]
        if (valueField !is JList && valueField !is JObject && fields.size == 1) {
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
                        .mapIndexed { index, s -> (if (index > 0) "  " else "") + s }.joinToString("\n")
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
class JList(val objects: MutableList<out JObject>) : JObject(mutableMapOf("value" to objects)) {
    override fun toString(): String {
        val sb = StringBuilder()
        fields.map { (_, jObject) -> jObject.toString() }.joinToString(",\n")
                .let { sb.append(it) }
        return sb.toString()
    }
}

class JFunction(val name: String, val code: String, val acceptedArguments: MutableList<JFunctionArgument>, val returnInfo: JFunctionReturnInfo) :
        JObject(mutableMapOf("name" to name, "arguments" to JList(acceptedArguments), "returns" to returnInfo,
                "code" to code)) {

    override fun toString(): String {
        // val obj = JObject(mutableMapOf("type" to "function", "accepts"))
        return "@func "
    }
}

class JFunctionArgument(val name: String, val required: Boolean, val type: ObjectType)
    : JObject(mutableMapOf("name" to name, "required" to required, "type" to type))

class JFunctionReturnInfo(val returnType: ObjectType, val required: Boolean)
    :JObject(mutableMapOf("returnType" to returnType, "required" to required))
/**
 * Parent number class. Accepted are 2 types: longs and doubles, nothing else
 */
open class JNumber(val backedValue: Number) : JObject(mutableMapOf("value" to backedValue))

class JLong(number: Long) : JNumber(number)
class JDouble(number: Double) : JNumber(number)

class JString(val string: String) : JObject(mutableMapOf("value" to string))

enum class ObjectType { STRING, LONG, DOUBLE, LIST, OBJECT, FUNCTION }