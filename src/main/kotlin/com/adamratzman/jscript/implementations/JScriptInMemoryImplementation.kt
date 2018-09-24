package com.adamratzman.jscript.implementations

import com.adamratzman.jscript.main.JScript
import com.adamratzman.jscript.variables.JFunction
import com.adamratzman.jscript.variables.JObject

class JScriptInMemoryImplementation : JScript() {
    val integratedObjects = mutableListOf<Class<out JObject>>()
    internal val variables = mutableMapOf<String?, MutableMap<String, JObject>>()

    override fun getGlobalVariable(identifier: String?, varName: String): JObject? {
        return variables[identifier]?.get(varName)
    }

    /**
     * By default, existing variables are overwritten
     */
    override fun saveGlobalVariable(identifier: String?, varName: String, value: JObject) {
        if (!variables.containsKey(identifier)) variables[identifier] = mutableMapOf()
        variables[identifier]!![varName] = value

    }

    override fun getExternalFunction(owner: String?, name: String): JFunction? {
        return functions[owner]?.find { it.name == name }
    }

    override fun saveFunction(owner: String?, jFunction: JFunction, external: Boolean) {
        if (!functions.containsKey(owner)) functions[owner] = mutableListOf()
        val functionsList = functions[owner]!!
        functionsList.find { it.name == jFunction.name }?.let {
            functionsList.remove(it)
        }
        functionsList.add(jFunction)
    }
}