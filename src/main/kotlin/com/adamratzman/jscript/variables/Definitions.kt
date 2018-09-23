package com.adamratzman.jscript.variables

class JDeserializer(val strict: Boolean) {
    fun fromString(string: String): JObject {
        if (strict && string.trim() != string) throw DeserializationException("Strict parsing is on and there's a leading or trailing space!")
        else {
            val toParse = string.trim()
            if (toParse.startsWith("\"")) {
                if (toParse.endsWith("\"")) return JString(toParse.substring(1, toParse.length - 1))
                else throw DeserializationException("The string provided doesn't have an end quotation")
            } else if (toParse.toDoubleOrNull() != null) {
                return JDouble(toParse.toDouble())
            } else if (toParse.toLongOrNull() != null) {
                return JLong(toParse.toLong())
            } else if (toParse.startsWith("[")) {
                val listObjectString = toParse.substring(1, toParse.length - 1)
                return JList(parseList(listObjectString))
            } else if (toParse.startsWith("{")) {
                return JString("asdfkasdf")
            } else throw DeserializationException("Object began with an unrecognized character")
        }
    }

    fun parseList(initList: String): MutableList<JObject> {
        val listObjectString = initList.trim()
        when (listObjectString[0]) {
            // if item is a string
            '\"' -> {
                for (i in 1..(listObjectString.length - 1)) {
                    if (listObjectString[i] == '\"' && (i == 0 || listObjectString[i - 1] != '\\')) {
                        return if (i == listObjectString.lastIndex) mutableListOf(fromString(listObjectString.substring(0, i + 1)))
                        else if (listObjectString[i + 1] == ',') {
                            (mutableListOf(fromString(listObjectString.substring(0, i + 1))) + parseList(listObjectString.substring(2, listObjectString.length))).toMutableList()
                        } else throw DeserializationException("A character other than a comma was provided as the delimeter in $listObjectString at position ${i + 1}")
                    }
                }
                throw DeserializationException("A matching quotation mark wasn't found for $listObjectString")
            }
            // if item is an object or another list
            '{', '[' -> {
                val leftChar = listObjectString[0]
                val rightChar = if (leftChar == '{') '}' else ']'
                var leftCount = 1
                var rightCount = 0
                for (i in 1..(listObjectString.length - 1)) {
                    if (listObjectString[i] == leftChar) leftCount++
                    else if (listObjectString[i] == rightChar) rightCount++
                    if (leftCount == rightCount) {
                        val leftObj = listObjectString.substring(0, i + 1)
                        return when {
                            i == listObjectString.lastIndex -> mutableListOf(fromString(leftObj))
                            listObjectString[i + 1] == ',' -> (mutableListOf(fromString(leftObj)) + parseList(listObjectString.substring(i + 2, listObjectString.length))).toMutableList()
                            else -> throw DeserializationException("A character other than a comma was provided as the delimeter in $listObjectString at position ${i + 1}")
                        }
                    }
                }
                if (leftCount != rightCount) throw DeserializationException("A matching outer $rightChar wasn't found for $leftChar in $listObjectString")
            }
            // if item is a number
            else -> {
                val endParsePosition = listObjectString.indexOf(',').let { if (it == -1) listObjectString.length else it }
                val num = listObjectString.substring(0, endParsePosition).let {
                    it.toLongOrNull() ?:it.toDoubleOrNull()
                } ?: throw DeserializationException("Expected number in $listObjectString characters 0 to ${endParsePosition - 1}")
                val obj = when (num) {
                    is Long -> JLong(num)
                    is Double -> JDouble(num)
                    else -> throw DeserializationException("Expected type Double or Long in $listObjectString. Got... something else?")
                }
                return when {
                    endParsePosition == listObjectString.length -> mutableListOf(obj)
                    listObjectString[endParsePosition] == ',' -> (mutableListOf(obj) + parseList(listObjectString.substring(endParsePosition + 1, listObjectString.length))) as MutableList<JObject>
                    else -> throw DeserializationException("Expected a comma after term in $listObjectString.")
                }
            }
        }
        return mutableListOf()
    }
}

fun MutableList<JObject>.getJListFromParsedList() = this[0] as JList

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
    : JObject(mutableMapOf("returnType" to returnType, "required" to required))

/**
 * Parent number class. Accepted are 2 types: longs and doubles, nothing else
 */
open class JNumber(val backedValue: Number) : JObject(mutableMapOf("value" to backedValue))

class JLong(number: Long) : JNumber(number)
class JDouble(number: Double) : JNumber(number)

class JString(val string: String) : JObject(mutableMapOf("value" to string))

enum class ObjectType { STRING, LONG, DOUBLE, LIST, OBJECT, FUNCTION }

class DeserializationException(message: String) : Exception(message)