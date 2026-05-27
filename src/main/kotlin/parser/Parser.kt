package parser

import exception.ParserException
import exception.ArrayTypeMismatchException
import org.example.lexer.enums.TokenType
import org.example.lexer.models.Token
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

class Parser(private val tokens: List<Token>) {
    private var position = 0
    private val variables = mutableListOf<VarStatement>()
    private val variableTypes = mutableMapOf<String, String?>()
    val errors = mutableListOf<String>()

    fun parse(): List<Statement> {
        val statements = mutableListOf<Statement>()
        while (!isAtEnd()) {
            try {
                statements.add(parseDeclaration())
            } catch (e: ParserException) {
                errors.add(e.message ?: "Unknown error")
                synchronize()
            }
        }
        checkVariableUsage()
        return statements
    }

    private fun checkVariableUsage() {
        for (variable in variables) {
            if (!variable.isUsed) {
                errors.add("Variable '${variable.name}' is not used")
            }
            if (!variable.isInitialized) {
                errors.add("Variable '${variable.name}' is not initialized")
            }
        }
    }

    private fun parseDeclaration(): Statement {
        if (match(TokenType.FUN)) return parseFunctionDeclaration()
        if (match(TokenType.VAR)) return parseVarDeclaration()
        return parseStatement()
    }

    private fun parseStatement(): Statement {
        if (match(TokenType.IF)) return parseIfStatement()
        if (match(TokenType.WHILE)) return parseWhileStatement()
        if (match(TokenType.RETURN)) return parseReturnStatement()
        if (match(TokenType.PRINT)) return parsePrintStatement()
        if (match(TokenType.LBRACE)) return BlockStatement(parseBlock())
        return parseExpressionStatement()
    }

    private fun parseFunctionDeclaration(): Statement {
        val name = consume(TokenType.ID, "Expected function name.")
        consume(TokenType.LPAREN, "Expected '(' after function name.")

        val parameters = mutableListOf<String>()
        if (!check(TokenType.RPAREN)) {
            do {
                parameters.add(consume(TokenType.ID, "Expected parameter name.").value)
            } while (match(TokenType.COMMA))
        }

        consume(TokenType.RPAREN, "Expected ')' after parameters.")
        consume(TokenType.LBRACE, "Expected '{' before function body.")
        return FunctionStatement(name.value, parameters, BlockStatement(parseBlock()))
    }

    private fun parseVarDeclaration(): Statement {
        val name = consume(TokenType.ID, "Expected variable name.")
        val declaredType = if (match(TokenType.COLON)) parseTypeName() else null
        var initializer: Expression? = null

        if (match(TokenType.EQ)) {
            initializer = parseExpression()
        }

        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration.")
        val varStatement = VarStatement(name.value, declaredType, initializer)
        variables.add(varStatement)
        variableTypes[name.value] = declaredType ?: initializer?.let { inferExpressionType(it) }
        return varStatement
    }

    private fun parseTypeName(): String {
        val typeToken = consume(TokenType.ID, "Expected variable type after ':'.")
        val typeName = typeToken.value
        if (typeName != "number" && typeName != "string" && typeName != "boolean") {
            throw ParserException("Unknown type '$typeName'. Supported types: number, string, boolean.", typeToken.line)
        }
        return typeName
    }

    private fun parseIfStatement(): Statement {
        consume(TokenType.LPAREN, "Expected '(' after 'if'.")
        val condition = parseExpression()
        consume(TokenType.RPAREN, "Expected ')' after 'if' condition.")

        val thenBranch = parseStatement()
        var elseBranch: Statement? = null

        if (match(TokenType.ELSE)) {
            elseBranch = parseStatement()
        }

        return IfStatement(condition, thenBranch, elseBranch)
    }

    private fun parseWhileStatement(): Statement {
        consume(TokenType.LPAREN, "Expected '(' after 'while'.")
        val condition = parseExpression()
        consume(TokenType.RPAREN, "Expected ')' after 'while' condition.")

        val body = parseStatement()
        return WhileStatement(condition, body)
    }

    private fun parsePrintStatement(): Statement {
        val value = parseExpression()
        consume(TokenType.SEMICOLON, "Expected ';' after value.")
        return PrintStatement(value)
    }

    private fun parseReturnStatement(): Statement {
        val value = if (!check(TokenType.SEMICOLON)) parseExpression() else null
        consume(TokenType.SEMICOLON, "Expected ';' after return value.")
        return ReturnStatement(value)
    }

    private fun parseExpressionStatement(): Statement {
        val expr = parseExpression()
        consume(TokenType.SEMICOLON, "Expected ';' after expression.")
        return ExpressionStatement(expr)
    }

    private fun parseBlock(): List<Statement> {
        val statements = mutableListOf<Statement>()

        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            statements.add(parseDeclaration())
        }

        consume(TokenType.RBRACE, "Expected '}' after block.")
        return statements
    }

    private fun parseExpression(): Expression {
        return parseAssignment()
    }

    private fun parseAssignment(): Expression {
        val expr = parseLogicalOr()

        if (match(TokenType.EQ)) {
            val equals = previous()
            val value = parseAssignment()

            if (expr is VariableExpression) {
                val variable = variables.find { it.name == expr.name }
                variable?.isInitialized = true
                variableTypes[expr.name] = inferExpressionType(value)
                return AssignExpression(expr.name, value)
            }

            if (expr is IndexExpression) {
                return IndexAssignExpression(expr.array, expr.index, value)
            }

            throw ParserException("Invalid assignment target.", equals.line)
        }

        return expr
    }

    private fun parseLogicalOr(): Expression {
        var expr = parseLogicalAnd()

        while (match(TokenType.OR)) {
            val op = previous().tokenType
            val right = parseLogicalAnd()
            expr = BinaryExpression(expr, op, right)
        }

        return expr
    }

    private fun parseLogicalAnd(): Expression {
        var expr = parseEquality()

        while (match(TokenType.AND)) {
            val op = previous().tokenType
            val right = parseEquality()
            expr = BinaryExpression(expr, op, right)
        }

        return expr
    }

    private fun parseEquality(): Expression {
        var expr = parseComparison()

        while (match(TokenType.EQEQ, TokenType.NEQ)) {
            val op = previous().tokenType
            val right = parseComparison()
            expr = BinaryExpression(expr, op, right)
        }

        return expr
    }

    private fun parseComparison(): Expression {
        var expr = parseTerm()

        while (match(TokenType.LT, TokenType.LTEQ, TokenType.GT, TokenType.GREQ)) {
            val op = previous().tokenType
            val right = parseTerm()
            expr = BinaryExpression(expr, op, right)
        }

        return expr
    }

    private fun parseTerm(): Expression {
        var expr = parseFactor()

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val op = previous().tokenType
            val right = parseFactor()
            expr = BinaryExpression(expr, op, right)
        }

        return expr
    }

    private fun parseFactor(): Expression {
        var expr = parseUnary()

        while (match(TokenType.STAR, TokenType.SLASH)) {
            val op = previous().tokenType
            val right = parseUnary()
            expr = BinaryExpression(expr, op, right)
        }

        return expr
    }

    private fun parseUnary(): Expression {
        if (match(TokenType.EXCL, TokenType.MINUS)) {
            val op = previous().tokenType
            val right = parseUnary()
            return UnaryExpression(op, right)
        }

        return parseCall()
    }

    private fun parseCall(): Expression {
        var expr = parsePrimary()

        while (true) {
            if (match(TokenType.LPAREN)) {
                val arguments = mutableListOf<Expression>()
                if (!check(TokenType.RPAREN)) {
                    do {
                        arguments.add(parseExpression())
                    } while (match(TokenType.COMMA))
                }
                consume(TokenType.RPAREN, "Expected ')' after arguments.")

                if (expr !is VariableExpression) {
                    throw ParserException("Expected function name before '('.", previous().line)
                }
                expr = CallExpression(expr.name, arguments)
            } else if (match(TokenType.LBRACKET)) {
                val index = parseExpression()
                consume(TokenType.RBRACKET, "Expected ']' after index.")
                expr = IndexExpression(expr, index)
            } else {
                break
            }
        }

        return expr
    }

    private fun parsePrimary(): Expression {
        if (match(TokenType.STRING)) {
            val value = previous().value
            return StringExpression(value)
        }

        if (match(TokenType.TRUE)) {
            return BooleanExpression(true)
        }

        if (match(TokenType.FALSE)) {
            return BooleanExpression(false)
        }

        if (match(TokenType.NUMBER)) {
            val value = previous().value.toDouble()
            return NumberExpression(value)
        }

        if (match(TokenType.ID)) {
            val variableName = previous().value
            val variable = variables.find { it.name == variableName }
            variable?.isUsed = true
            return VariableExpression(variableName)
        }

        if (match(TokenType.LPAREN)) {
            val expr = parseExpression()
            consume(TokenType.RPAREN, "Expected ')' after expression.")
            return expr
        }

        if (match(TokenType.LBRACKET)) {
            val elements = mutableListOf<Expression>()
            if (!check(TokenType.RBRACKET)) {
                do {
                    elements.add(parseExpression())
                } while (match(TokenType.COMMA))
            }
            consume(TokenType.RBRACKET, "Expected ']' after array elements.")
            checkArrayElementTypes(elements)
            return ArrayExpression(elements)
        }

        throw ParserException("Expected expression.", peek().line)
    }

    private fun checkArrayElementTypes(elements: List<Expression>) {
        var expectedType: String? = null
        for (element in elements) {
            val elementType = inferExpressionType(element)
            if (elementType == null) {
                continue
            }
            if (expectedType == null) {
                expectedType = elementType
                continue
            }
            if (elementType != expectedType) {
                throw ArrayTypeMismatchException(peek().line)
            }
        }
    }

    private fun inferExpressionType(expression: Expression): String? {
        return when (expression) {
            is NumberExpression -> "number"
            is StringExpression -> "string"
            is BooleanExpression -> "boolean"
            is ArrayExpression -> {
                val elementType = expression.elements.firstOrNull()?.let { inferExpressionType(it) }
                elementType?.let { "array<$it>" } ?: "array"
            }
            is VariableExpression -> variableTypes[expression.name]
            is UnaryExpression -> inferExpressionType(expression.right)
            is BinaryExpression -> inferBinaryExpressionType(expression)
            is IndexExpression -> {
                val arrayType = inferExpressionType(expression.array)
                if (arrayType?.startsWith("array<") == true && arrayType.endsWith(">")) {
                    arrayType.removePrefix("array<").removeSuffix(">")
                } else {
                    null
                }
            }
            else -> null
        }
    }

    private fun inferBinaryExpressionType(expression: BinaryExpression): String? {
        val leftType = inferExpressionType(expression.left)
        val rightType = inferExpressionType(expression.right)
        return when (expression.operator) {
            TokenType.PLUS -> if (leftType == "string" || rightType == "string") "string" else "number"
            TokenType.MINUS, TokenType.STAR, TokenType.SLASH -> "number"
            TokenType.EQEQ, TokenType.NEQ,
            TokenType.LT, TokenType.LTEQ, TokenType.GT, TokenType.GREQ,
            TokenType.AND, TokenType.OR -> "boolean"
            else -> null
        }
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().tokenType == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) position++
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().tokenType == TokenType.EOF

    private fun peek(): Token = tokens[position]

    private fun previous(): Token = tokens[position - 1]

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        val token = peek()
        throw ParserException(message, token.line)
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().tokenType == TokenType.SEMICOLON) return

            when (peek().tokenType) {
                TokenType.VAR, TokenType.FUN, TokenType.RETURN, TokenType.PRINT, TokenType.IF, TokenType.WHILE -> return
                else -> advance()
            }
        }
    }
}
