package interpreter

import org.example.lexer.enums.TokenType
import parser.ast.expression.*
import parser.ast.statement.*

class Interpreter {
    private var environment = Environment()
    private val output = StringBuilder()
    
    fun interpret(statements: List<Statement>) {
        for (statement in statements) {
            execute(statement)
        }
    }
    
    fun getOutput(): String = output.toString()
    
    private fun execute(statement: Statement) {
        when (statement) {
            is VarStatement -> executeVarStatement(statement)
            is PrintStatement -> executePrintStatement(statement)
            is IfStatement -> executeIfStatement(statement)
            is WhileStatement -> executeWhileStatement(statement)
            is BlockStatement -> executeBlockStatement(statement)
            is ExpressionStatement -> evaluate(statement.expression)
        }
    }
    
    private fun executeVarStatement(statement: VarStatement) {
        val value = if (statement.initializer != null) {
            evaluate(statement.initializer)
        } else {
            ValueType.NullValue
        }
        environment.define(statement.name, value)
    }
    
    private fun executePrintStatement(statement: PrintStatement) {
        val value = evaluate(statement.expression)
        output.appendLine(value.toString())
    }
    
    private fun executeIfStatement(statement: IfStatement) {
        val condition = evaluate(statement.condition)
        if (isTruthy(condition)) {
            execute(statement.thenBranch)
        } else if (statement.elseBranch != null) {
            execute(statement.elseBranch)
        }
    }
    
    private fun executeWhileStatement(statement: WhileStatement) {
        while (isTruthy(evaluate(statement.condition))) {
            execute(statement.body)
        }
    }
    
    private fun executeBlockStatement(statement: BlockStatement) {
        val parentEnvironment = environment
        environment = environment.createChild()
        try {
            for (stmt in statement.statements) {
                execute(stmt)
            }
        } finally {
            environment = parentEnvironment
        }
    }
    
    private fun evaluate(expression: Expression): ValueType {
        return when (expression) {
            is NumberExpression -> ValueType.NumberValue(expression.value)
            is StringExpression -> ValueType.StringValue(expression.value)
            is BooleanExpression -> ValueType.BooleanValue(expression.value)
            is VariableExpression -> evaluateVariable(expression)
            is UnaryExpression -> evaluateUnary(expression)
            is BinaryExpression -> evaluateBinary(expression)
            is AssignExpression -> evaluateAssignment(expression)
            else -> throw RuntimeException("Неизвестное выражение: ${expression::class.simpleName}")
        }
    }
    
    private fun evaluateVariable(expression: VariableExpression): ValueType {
        return environment.get(expression.name)
    }
    
    private fun evaluateUnary(expression: UnaryExpression): ValueType {
        val right = evaluate(expression.right)
        
        return when (expression.operator) {
            TokenType.MINUS -> {
                when (right) {
                    is ValueType.NumberValue -> ValueType.NumberValue(-right.value)
                    else -> throw RuntimeException("Ожидается число для унарного минуса")
                }
            }
            TokenType.EXCL -> {
                ValueType.BooleanValue(!isTruthy(right))
            }
            else -> throw RuntimeException("Неизвестный унарный оператор: ${expression.operator}")
        }
    }
    
    private fun evaluateBinary(expression: BinaryExpression): ValueType {
        val left = evaluate(expression.left)
        val right = evaluate(expression.right)
        
        return when (expression.operator) {
            TokenType.PLUS -> {
                when {
                    left is ValueType.NumberValue && right is ValueType.NumberValue -> 
                        ValueType.NumberValue(left.value + right.value)
                    left is ValueType.StringValue || right is ValueType.StringValue -> 
                        ValueType.StringValue(left.toString() + right.toString())
                    else -> throw RuntimeException("Недопустимые операнды для '+'")
                }
            }
            TokenType.MINUS -> {
                requireBothNumbers(left, right, "-")
                ValueType.NumberValue((left as ValueType.NumberValue).value - (right as ValueType.NumberValue).value)
            }
            TokenType.STAR -> {
                requireBothNumbers(left, right, "*")
                ValueType.NumberValue((left as ValueType.NumberValue).value * (right as ValueType.NumberValue).value)
            }
            TokenType.SLASH -> {
                requireBothNumbers(left, right, "/")
                val rightNum = right as ValueType.NumberValue
                if (rightNum.value == 0.0) throw RuntimeException("Деление на ноль")
                ValueType.NumberValue((left as ValueType.NumberValue).value / rightNum.value)
            }
            TokenType.LT -> {
                requireBothNumbers(left, right, "<")
                ValueType.BooleanValue((left as ValueType.NumberValue).value < (right as ValueType.NumberValue).value)
            }
            TokenType.LTEQ -> {
                requireBothNumbers(left, right, "<=")
                ValueType.BooleanValue((left as ValueType.NumberValue).value <= (right as ValueType.NumberValue).value)
            }
            TokenType.GT -> {
                requireBothNumbers(left, right, ">")
                ValueType.BooleanValue((left as ValueType.NumberValue).value > (right as ValueType.NumberValue).value)
            }
            TokenType.GREQ -> {
                requireBothNumbers(left, right, ">=")
                ValueType.BooleanValue((left as ValueType.NumberValue).value >= (right as ValueType.NumberValue).value)
            }
            TokenType.EQEQ -> {
                ValueType.BooleanValue(valuesEqual(left, right))
            }
            TokenType.NEQ -> {
                ValueType.BooleanValue(!valuesEqual(left, right))
            }
            TokenType.AND -> {
                ValueType.BooleanValue(isTruthy(left) && isTruthy(right))
            }
            TokenType.OR -> {
                ValueType.BooleanValue(isTruthy(left) || isTruthy(right))
            }
            else -> throw RuntimeException("Неизвестный бинарный оператор: ${expression.operator}")
        }
    }
    
    private fun evaluateAssignment(expression: AssignExpression): ValueType {
        val value = evaluate(expression.value)
        environment.assign(expression.name, value)
        return value
    }
    
    private fun isTruthy(value: ValueType): Boolean {
        return when (value) {
            is ValueType.BooleanValue -> value.value
            is ValueType.NumberValue -> value.value != 0.0
            is ValueType.StringValue -> value.value.isNotEmpty()
            ValueType.NullValue -> false
        }
    }
    
    private fun valuesEqual(a: ValueType, b: ValueType): Boolean {
        return when {
            a is ValueType.NullValue && b is ValueType.NullValue -> true
            a is ValueType.NumberValue && b is ValueType.NumberValue -> a.value == b.value
            a is ValueType.StringValue && b is ValueType.StringValue -> a.value == b.value
            a is ValueType.BooleanValue && b is ValueType.BooleanValue -> a.value == b.value
            else -> false
        }
    }
    
    private fun requireBothNumbers(left: ValueType, right: ValueType, operator: String) {
        if (left !is ValueType.NumberValue || right !is ValueType.NumberValue) {
            throw RuntimeException("Операнды для '$operator' должны быть числами")
        }
    }
}
