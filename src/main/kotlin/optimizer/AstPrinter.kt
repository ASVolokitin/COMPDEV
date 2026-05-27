package optimizer

import org.example.lexer.enums.TokenType
import parser.ast.expression.AssignExpression
import parser.ast.expression.ArrayExpression
import parser.ast.expression.BinaryExpression
import parser.ast.expression.BooleanExpression
import parser.ast.expression.CallExpression
import parser.ast.expression.Expression
import parser.ast.expression.IndexAssignExpression
import parser.ast.expression.IndexExpression
import parser.ast.expression.NumberExpression
import parser.ast.expression.StringExpression
import parser.ast.expression.UnaryExpression
import parser.ast.expression.VariableExpression
import parser.ast.statement.BlockStatement
import parser.ast.statement.ExpressionStatement
import parser.ast.statement.FunctionStatement
import parser.ast.statement.IfStatement
import parser.ast.statement.PrintStatement
import parser.ast.statement.ReturnStatement
import parser.ast.statement.Statement
import parser.ast.statement.VarStatement
import parser.ast.statement.WhileStatement

class AstPrinter {
    fun print(statements: List<Statement>): String {
        return statements.joinToString(separator = "\n") { printStatement(it) }
    }

    private fun printStatement(statement: Statement): String {
        return when (statement) {
            is VarStatement -> {
                val type = statement.declaredType?.let { ": $it" }.orEmpty()
                val initializer = statement.initializer?.let { " = ${printExpression(it)}" }.orEmpty()
                "var ${statement.name}$type$initializer;"
            }
            is PrintStatement -> "print ${printExpression(statement.expression)};"
            is ExpressionStatement -> "${printExpression(statement.expression)};"
            is IfStatement -> {
                val elseBranch = statement.elseBranch?.let { " else ${printStatement(it)}" }.orEmpty()
                "if (${printExpression(statement.condition)}) ${printStatement(statement.thenBranch)}$elseBranch"
            }
            is WhileStatement -> "while (${printExpression(statement.condition)}) ${printStatement(statement.body)}"
            is BlockStatement -> statement.statements.joinToString(
                prefix = "{ ",
                separator = " ",
                postfix = " }"
            ) { printStatement(it) }
            is FunctionStatement -> {
                val parameters = statement.parameters.joinToString(", ")
                "fun ${statement.name}($parameters) ${printStatement(statement.body)}"
            }
            is ReturnStatement -> {
                val value = statement.value?.let { " ${printExpression(it)}" }.orEmpty()
                "return$value;"
            }
            else -> statement::class.simpleName ?: "UnknownStatement"
        }
    }

    private fun printExpression(expression: Expression): String {
        return when (expression) {
            is NumberExpression -> formatNumber(expression.value)
            is StringExpression -> "\"${escapeString(expression.value)}\""
            is BooleanExpression -> expression.value.toString()
            is VariableExpression -> expression.name
            is UnaryExpression -> "(${operatorText(expression.operator)} ${printExpression(expression.right)})"
            is BinaryExpression -> "(${printExpression(expression.left)} ${operatorText(expression.operator)} ${printExpression(expression.right)})"
            is AssignExpression -> "(${expression.name} = ${printExpression(expression.value)})"
            is ArrayExpression -> expression.elements.joinToString(
                prefix = "[",
                separator = ", ",
                postfix = "]"
            ) { printExpression(it) }
            is IndexExpression -> "${printExpression(expression.array)}[${printExpression(expression.index)}]"
            is IndexAssignExpression -> "(${printExpression(expression.array)}[${printExpression(expression.index)}] = ${printExpression(expression.value)})"
            is CallExpression -> {
                val arguments = expression.arguments.joinToString(", ") { printExpression(it) }
                "${expression.callee}($arguments)"
            }
            else -> expression::class.simpleName ?: "UnknownExpression"
        }
    }

    private fun operatorText(operator: TokenType): String {
        return when (operator) {
            TokenType.PLUS -> "+"
            TokenType.MINUS -> "-"
            TokenType.STAR -> "*"
            TokenType.SLASH -> "/"
            TokenType.EQ -> "="
            TokenType.EQEQ -> "=="
            TokenType.EXCL -> "!"
            TokenType.NEQ -> "!="
            TokenType.LT -> "<"
            TokenType.GT -> ">"
            TokenType.LTEQ -> "<="
            TokenType.GREQ -> ">="
            TokenType.AND -> "&&"
            TokenType.OR -> "||"
            else -> operator.name
        }
    }

    private fun formatNumber(value: Double): String {
        return if (value == value.toInt().toDouble()) {
            value.toInt().toString()
        } else {
            value.toString()
        }
    }

    private fun escapeString(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
    }
}
