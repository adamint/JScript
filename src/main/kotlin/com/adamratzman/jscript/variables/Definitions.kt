package com.adamratzman.jscript.variables

import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField

class JDeserializer(val strict: Boolean) {
    fun fromString(string: String): JObject {
        if (strict && string.trim() != string) throw DeserializationException("Strict parsing is on and there's a leading or trailing space!")
        else {
            val toParse = string.trim()
            return if (toParse.startsWith("\"")) {
                if (toParse.endsWith("\"")) JString(toParse.substring(1, toParse.length - 1))
                else throw DeserializationException("The string provided doesn't have an end quotation")
            } else if (toParse.toDoubleOrNull() != null) {
                JDouble(toParse.toDouble())
            } else if (toParse.toLongOrNull() != null) {
                JLong(toParse.toLong())
            } else if (toParse == "false" || toParse == "true") {
                JBoolean(toParse.toBoolean())
            } else if (toParse == "nothing") {
                JNothing()
            } else if (toParse.startsWith("[")) {
                val listObjectString = toParse.substring(1, toParse.length - 1)
                JList(parseList(listObjectString))
            } else if (toParse.startsWith("{")) {
                return parseObject(toParse)
            } else throw DeserializationException("Object began with an unrecognized character")
        }
    }

    fun parseObject(untrimmedString: String): JObject {
        val objectString = untrimmedString.trim()
        if (!objectString.endsWith("}")) throw DeserializationException("Object $objectString must end with a closing }")
        val keyValuePairs = mutableMapOf<String, Any>()
        var innerString = objectString.substring(1, objectString.lastIndex).trim()
        while (innerString.isNotEmpty()) {
            if (innerString.startsWith(",") || innerString.startsWith(" ")) innerString = innerString.substring(1).trim()
            val split = innerString.split(" ")
            if (split[1] != "=") throw DeserializationException("Object keys must be mapped to a value with the = sign in $innerString")
            val key = split[0]
            if (key[0].isDigit()) throw DeserializationException("Key $key cannot start with a number in $innerString")
            if (key == "nothing" || key == "=") throw DeserializationException("Key in $innerString cannot be named nothing or =")
            val toParse = split.subList(2, split.size).joinToString(" ").trim()
            when (toParse[0]) {
                // string value
                '\"' -> {
                    var found = false
                    for (i in 1..(toParse.length - 1)) {
                        if (toParse[i] == '\"' && (i == 1 || toParse[i - 1] != '\\')) {
                            val value = toParse.substring(1, i)
                            keyValuePairs[key] = value
                            found = true
                            innerString = toParse.substring(i + 1, toParse.length)
                            break
                        }
                    }
                    if (!found) throw DeserializationException("A matching quote was expected in $toParse")
                }
                '{', '[' -> {
                    val leftChar = toParse[0]
                    val rightChar = if (leftChar == '{') '}' else ']'
                    var leftCount = 1
                    var rightCount = 0

                    for (i in 1..(toParse.length - 1)) {
                        if (toParse[i] == leftChar) leftCount++
                        else if (toParse[i] == rightChar) rightCount++
                        if (leftCount == rightCount) {
                            val leftObj = toParse.substring(0, i + 1).trim()
                            keyValuePairs[key] = if (leftObj[0] == '{') parseObject(leftObj) else JList(parseList(leftObj)).objects[0]
                            innerString = if (i == toParse.lastIndex) "" else toParse.substring(i + 1)
                            break
                        }
                    }

                    if (leftCount != rightCount) throw DeserializationException("A matching outer $rightChar wasn't found for $leftChar in $toParse")
                }
                // boolean, number, or nothing value
                else -> {
                    val endParsePosition = toParse.indexOf(',').let { if (it == -1) toParse.length else it }
                    val parsed = toParse.substring(0, endParsePosition).let {
                        if (it == "false" || it == "true") it.toBoolean() else if (it == "nothing") Unit else it.toLongOrNull() ?: it.toDoubleOrNull()
                    }
                            ?: throw DeserializationException("Expected number in $toParse characters 0 to ${endParsePosition - 1}")
                    val obj = when (parsed) {
                        is Unit -> JNothing()
                        is Boolean -> JBoolean(parsed)
                        is Long -> JLong(parsed)
                        is Double -> JDouble(parsed)
                        else -> throw DeserializationException("Expected type Double, Long, Nothing, or Boolean in $toParse. Got... something else?")
                    }
                    keyValuePairs[key] = (obj as? JNumber)?.backedValue ?: if (obj is JNothing) Unit else (obj as JBoolean).boolean
                    innerString = if (endParsePosition == toParse.length || toParse[endParsePosition] != ',') "" else toParse.substring(endParsePosition + 1)
                }
            }
        }
        return JObject(keyValuePairs)
    }

    fun parseList(initList: String): MutableList<JObject> {
        val listObjectString = initList.trim()
        when (listObjectString[0]) {
            // if item is a string
            '\"' -> {
                for (i in 1..(listObjectString.length - 1)) {
                    if (listObjectString[i] == '\"' && (i == 1 || listObjectString[i - 1] != '\\')) {
                        return when {
                            i == listObjectString.lastIndex -> mutableListOf(fromString(listObjectString.substring(0, i + 1)))
                            listObjectString[i + 1] == ',' -> (mutableListOf(fromString(listObjectString.substring(0, i + 1))) + parseList(listObjectString.substring(i + 2, listObjectString.length))).toMutableList()
                            else -> throw DeserializationException("A character other than a comma was provided as the delimeter in $listObjectString at position ${i + 1}")
                        }
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
            // if item is a boolean, number, or nothing
            else -> {
                val endParsePosition = listObjectString.indexOf(',').let { if (it == -1) listObjectString.length else it }

                val parsed = listObjectString.substring(0, endParsePosition).let {
                    if (it == "false" || it == "true") it.toBoolean() else if (it == "nothing") Unit else it.toLongOrNull() ?: it.toDoubleOrNull()
                }
                        ?: throw DeserializationException("Expected number in $listObjectString characters 0 to ${endParsePosition - 1}")
                val obj = when (parsed) {
                    is Unit -> JNothing()
                    is Boolean -> JBoolean(parsed)
                    is Long -> JLong(parsed)
                    is Double -> JDouble(parsed)
                    else -> throw DeserializationException("Expected type Double, Long, Nothing, or Boolean in $listObjectString. Got... something else?")
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

open class JObject(var fields: MutableMap<String, Any> = mutableMapOf()) {
    fun setupObject() {
        this::class.declaredMemberProperties.forEach { property ->
            property.javaField?.let { it.isAccessible = true; println(it.get(this)) }
            property.getter.call(this)?.let { if (property.name != "fields") fields[property.name] = it }
        }
    }

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
                is Boolean -> value.toString()
                is Unit -> "nothing"
                else -> "\"$value\""
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
        JObject() {
    init {
        setupObject()
    }

    override fun toString(): String {
        // val obj = JObject(mutableMapOf("type" to "function", "accepts"))
        return "@func "
    }
}

class JFunctionArgument(val name: String, val required: Boolean, val type: ObjectType) : JObject() {
    init {
        setupObject()
    }
}

class JFunctionReturnInfo(val returnType: ObjectType, val required: Boolean) : JObject() {
    init {
        setupObject()
    }
}

/**
 * Parent number class. Accepted are 2 types: longs and doubles, nothing else
 */
open class JNumber(val backedValue: Number) : JObject(mutableMapOf("value" to backedValue))

class JLong(number: Long) : JNumber(number)
class JDouble(number: Double) : JNumber(number)

class JString(val string: String) : JObject(mutableMapOf("value" to string))

class JBoolean(val boolean: Boolean) : JObject(mutableMapOf("value" to boolean))

class JNothing:JObject(mutableMapOf("value" to Unit))

enum class ObjectType { STRING, LONG, DOUBLE, LIST, OBJECT, FUNCTION }

class DeserializationException(message: String) : Exception(message)
class ScriptException(message: String) : Exception(message)