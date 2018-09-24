package com.adamratzman.jscript.main

import com.adamratzman.jscript.variables.JFunction
import com.adamratzman.jscript.variables.JObject

abstract class JScript {
    internal val functions = mutableMapOf<String?, MutableList<JFunction>>()

    abstract fun getGlobalVariable(identifier: String? /* null is global */, varName: String): JObject?
    abstract fun saveGlobalVariable(identifier: String?, varName: String, value: JObject)

    abstract fun getExternalFunction(owner: String?, name: String): JFunction?
    abstract fun saveFunction(owner: String?, jFunction: JFunction, external: Boolean)

    fun getFunction(owner: String?, name: String): JFunction? = functions[owner]?.find { it.name == name }
            ?: getExternalFunction(owner, name)
}