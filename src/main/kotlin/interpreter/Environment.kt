package interpreter

import parser.ast.statement.FunctionStatement

class Environment(private val parent: Environment? = null) {
    private val values = mutableMapOf<String, ValueType>()
    private val functions = mutableMapOf<String, FunctionStatement>()

    fun define(name: String, value: ValueType) {
        values[name] = value
    }

    fun get(name: String): ValueType {
        return values[name] ?: parent?.get(name) ?: throw RuntimeException("Variable '$name' not found")
    }

    fun assign(name: String, value: ValueType) {
        if (values.containsKey(name)) {
            values[name] = value
        } else if (parent != null) {
            parent.assign(name, value)
        } else {
            throw RuntimeException("Variable '$name' not found")
        }
    }

    fun defineFunction(name: String, function: FunctionStatement) {
        functions[name] = function
    }

    fun getFunction(name: String): FunctionStatement {
        return functions[name] ?: parent?.getFunction(name) ?: throw RuntimeException("Function '$name' not found")
    }

    fun createChild(): Environment {
        return Environment(this)
    }
}
