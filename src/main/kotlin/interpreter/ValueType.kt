package interpreter


sealed class ValueType {
    abstract val type: String
    
    data class NumberValue(val value: Double) : ValueType() {
        override val type: String = "number"
        override fun toString(): String {
            return if (value == value.toInt().toDouble()) {
                value.toInt().toString()
            } else {
                value.toString()
            }
        }
    }
    
    data class StringValue(val value: String) : ValueType() {
        override val type: String = "string"
        override fun toString() = value
    }
    
    data class BooleanValue(val value: Boolean) : ValueType() {
        override val type: String = "boolean"
        override fun toString() = value.toString()
    }

    data class ArrayValue(val elements: MutableList<ValueType>) : ValueType() {
        override val type: String = "array"
        override fun toString() = elements.joinToString(prefix = "[", postfix = "]")
    }
    
    object NullValue : ValueType() {
        override val type: String = "null"
        override fun toString() = "null"
    }
}
