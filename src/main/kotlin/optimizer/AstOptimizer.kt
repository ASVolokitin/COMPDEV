package optimizer

import org.example.lexer.enums.TokenType
import parser.ast.expression.AssignExpression
import parser.ast.expression.BinaryExpression
import parser.ast.expression.BooleanExpression
import parser.ast.expression.CallExpression
import parser.ast.expression.Expression
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

class AstOptimizer {
    fun optimize(statements: List<Statement>): List<Statement> {
        return optimizeStatements(statements, OptimizationContext())
    }

    private fun optimizeStatements(statements: List<Statement>, context: OptimizationContext): List<Statement> {
        return statements.map { optimizeStatement(it, context) }
    }

    private fun optimizeStatement(statement: Statement, context: OptimizationContext): Statement {
        return when (statement) {
            is VarStatement -> optimizeVarStatement(statement, context)
            is PrintStatement -> PrintStatement(optimizeExpression(statement.expression, context))
            is ExpressionStatement -> ExpressionStatement(optimizeExpression(statement.expression, context))
            is IfStatement -> optimizeIfStatement(statement, context)
            is WhileStatement -> WhileStatement(
                condition = optimizeExpression(statement.condition, context),
                body = optimizeStatement(statement.body, OptimizationContext())
            )
            is BlockStatement -> optimizeBlockStatement(statement, context)
            is FunctionStatement -> FunctionStatement(
                name = statement.name,
                parameters = statement.parameters,
                body = optimizeStatement(statement.body, OptimizationContext()) as BlockStatement
            )
            is ReturnStatement -> ReturnStatement(statement.value?.let { optimizeExpression(it, context) })
            else -> statement
        }
    }

    private fun optimizeVarStatement(statement: VarStatement, context: OptimizationContext): Statement {
        val initializer = statement.initializer?.let { optimizeExpression(it, context) }
        context.declare(statement.name, initializer?.takeIf { isConstant(it) })
        return VarStatement(
            name = statement.name,
            declaredType = statement.declaredType,
            initializer = initializer,
            isUsed = statement.isUsed,
            isInitialized = statement.isInitialized
        )
    }

    private fun optimizeIfStatement(statement: IfStatement, context: OptimizationContext): Statement {
        val condition = optimizeExpression(statement.condition, context)
        val thenBranch = optimizeStatement(statement.thenBranch, context.copy())
        val elseBranch = statement.elseBranch?.let { optimizeStatement(it, context.copy()) }
        context.clear()
        return IfStatement(condition, thenBranch, elseBranch)
    }

    private fun optimizeBlockStatement(statement: BlockStatement, context: OptimizationContext): Statement {
        val childContext = context.copy()
        childContext.pushScope()
        val optimizedStatements = optimizeStatements(statement.statements, childContext)
        context.replaceOuterScopesFrom(childContext)
        return BlockStatement(optimizedStatements)
    }

    private fun optimizeExpression(expression: Expression, context: OptimizationContext): Expression {
        return when (expression) {
            is BinaryExpression -> optimizeBinary(expression, context)
            is UnaryExpression -> optimizeUnary(expression, context)
            is AssignExpression -> optimizeAssign(expression, context)
            is CallExpression -> optimizeCall(expression, context)
            is VariableExpression -> context.get(expression.name) ?: expression
            is NumberExpression,
            is StringExpression,
            is BooleanExpression -> expression
            else -> expression
        }
    }

    private fun optimizeAssign(expression: AssignExpression, context: OptimizationContext): Expression {
        val value = optimizeExpression(expression.value, context)
        context.assign(expression.name, value.takeIf { isConstant(it) })
        return AssignExpression(expression.name, value)
    }

    private fun optimizeCall(expression: CallExpression, context: OptimizationContext): Expression {
        val arguments = expression.arguments.map { optimizeExpression(it, context) }
        context.clear()
        return CallExpression(expression.callee, arguments)
    }

    private fun optimizeBinary(expression: BinaryExpression, context: OptimizationContext): Expression {
        val left = optimizeExpression(expression.left, context)
        val right = optimizeExpression(expression.right, context)

        if (left is NumberExpression && right is NumberExpression) {
            return foldNumberBinary(left.value, expression.operator, right.value)
                ?: BinaryExpression(left, expression.operator, right)
        }

        if (expression.operator == TokenType.PLUS && (left is StringExpression || right is StringExpression) &&
            isConstant(left) && isConstant(right)
        ) {
            return StringExpression(stringValue(left) + stringValue(right))
        }

        return BinaryExpression(left, expression.operator, right)
    }

    private fun optimizeUnary(expression: UnaryExpression, context: OptimizationContext): Expression {
        val right = optimizeExpression(expression.right, context)
        if (expression.operator == TokenType.MINUS && right is NumberExpression) {
            return NumberExpression(-right.value)
        }
        return UnaryExpression(expression.operator, right)
    }

    private fun foldNumberBinary(left: Double, operator: TokenType, right: Double): NumberExpression? {
        return when (operator) {
            TokenType.PLUS -> NumberExpression(left + right)
            TokenType.MINUS -> NumberExpression(left - right)
            TokenType.STAR -> NumberExpression(left * right)
            TokenType.SLASH -> if (right == 0.0) null else NumberExpression(left / right)
            else -> null
        }
    }

    private fun isConstant(expression: Expression): Boolean {
        return expression is NumberExpression || expression is StringExpression || expression is BooleanExpression
    }

    private fun stringValue(expression: Expression): String {
        return when (expression) {
            is NumberExpression -> {
                if (expression.value == expression.value.toInt().toDouble()) {
                    expression.value.toInt().toString()
                } else {
                    expression.value.toString()
                }
            }
            is StringExpression -> expression.value
            is BooleanExpression -> expression.value.toString()
            else -> throw IllegalArgumentException("Expression is not a constant")
        }
    }

    private class OptimizationContext(
        private val scopes: MutableList<MutableMap<String, Expression?>> = mutableListOf(mutableMapOf())
    ) {
        fun get(name: String): Expression? {
            for (scope in scopes.asReversed()) {
                if (scope.containsKey(name)) {
                    return scope[name]
                }
            }
            return null
        }

        fun declare(name: String, value: Expression?) {
            scopes.last()[name] = value
        }

        fun assign(name: String, value: Expression?) {
            val scope = scopes.asReversed().firstOrNull { it.containsKey(name) } ?: scopes.last()
            scope[name] = value
        }

        fun pushScope() {
            scopes.add(mutableMapOf())
        }

        fun clear() {
            scopes.forEach { scope ->
                scope.keys.toList().forEach { name -> scope[name] = null }
            }
        }

        fun copy(): OptimizationContext {
            return OptimizationContext(scopes.map { it.toMutableMap() }.toMutableList())
        }

        fun replaceOuterScopesFrom(other: OptimizationContext) {
            scopes.clear()
            scopes.addAll(other.scopes.dropLast(1).map { it.toMutableMap() })
        }
    }
}
