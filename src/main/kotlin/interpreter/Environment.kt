package interpreter


class Environment(private val parent: Environment? = null) {
    private val values = mutableMapOf<String, ValueType>()

    fun define(name: String, value: ValueType) {
        values[name] = value
    }

    fun get(name: String): ValueType {
        return values[name] ?: parent?.get(name) ?: throw RuntimeException("Переменная '$name' не найдена")
    }

    fun assign(name: String, value: ValueType) {
        if (values.containsKey(name)) {
            values[name] = value
        } else if (parent != null) {
            parent.assign(name, value)
        } else {
            throw RuntimeException("Переменная '$name' не найдена")
        }
    }

    fun createChild(): Environment {
        return Environment(this)
    }
}
