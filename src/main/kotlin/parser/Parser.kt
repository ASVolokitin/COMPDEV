package org.example.parser

import org.example.lexer.enums.TokenType
import org.example.lexer.models.Token
import parser.ast.expression.AssignExpression
import parser.ast.expression.BinaryExpression
import parser.ast.expression.Expression
import parser.ast.expression.NumberExpression
import parser.ast.expression.UnaryExpression
import parser.ast.expression.VariableExpression
import parser.ast.statement.BlockStatement
import parser.ast.statement.ExpressionStatement
import parser.ast.statement.IfStatement
import parser.ast.statement.PrintStatement
import parser.ast.statement.Statement
import parser.ast.statement.VarStatement
import parser.ast.statement.WhileStatement

class Parser(tokens: Iterable<Token>) {
    private val tokens: List<Token> = tokens.toList()
    private var position: Int = 0

    fun parse(): List<Statement> {
        val statements = mutableListOf<Statement>()
        while (!isAtEnd()) {
            statements.add(parseDeclaration())
        }
        return statements
    }

    private fun parseDeclaration(): Statement {
        if (match(TokenType.VAR)) return parseVarDeclaration()
        return parseStatement()
    }

    private fun parseStatement(): Statement {
        if (match(TokenType.IF)) return parseIfStatement()
        if (match(TokenType.WHILE)) return parseWhileStatement()
        if (match(TokenType.PRINT)) return parsePrintStatement()
        if (match(TokenType.LBRACE)) return BlockStatement(parseBlock())
        return parseExpressionStatement()
    }

    private fun parseVarDeclaration(): Statement {
        val name = consume(TokenType.ID, "Ожидается имя переменной.")
        var initializer: Expression? = null

        if (match(TokenType.EQ)) {
            initializer = parseExpression()
        }

        consume(TokenType.SEMICOLON, "Ожидается ';' после объявления переменной.")
        return VarStatement(name.value, initializer)
    }

    private fun parseIfStatement(): Statement {
        consume(TokenType.LPAREN, "Ожидается '(' после 'if'.")
        val condition = parseExpression()
        consume(TokenType.RPAREN, "Ожидается ')' после условия 'if'.")

        val thenBranch = parseStatement()
        var elseBranch: Statement? = null

        if (match(TokenType.ELSE)) {
            elseBranch = parseStatement()
        }

        return IfStatement(condition, thenBranch, elseBranch)
    }

    private fun parseWhileStatement(): Statement {
        consume(TokenType.LPAREN, "Ожидается '(' после 'while'.")
        val condition = parseExpression()
        consume(TokenType.RPAREN, "Ожидается ')' после условия 'while'.")

        val body = parseStatement()
        return WhileStatement(condition, body)
    }

    private fun parsePrintStatement(): Statement {
        val value = parseExpression()
        consume(TokenType.SEMICOLON, "Ожидается ';' после значения.")
        return PrintStatement(value)
    }

    private fun parseExpressionStatement(): Statement {
        val expr = parseExpression()
        consume(TokenType.SEMICOLON, "Ожидается ';' после выражения.")
        return ExpressionStatement(expr)
    }

    private fun parseBlock(): List<Statement> {
        val statements = mutableListOf<Statement>()

        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            statements.add(parseDeclaration())
        }

        consume(TokenType.RBRACE, "Ожидается '}' после блока.")
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
                return AssignExpression(expr.name, value)
            }

            throw Exception("[Parser Error] Line ${equals.line}: Недопустимая цель для присваивания.")
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

        return parsePrimary()
    }

    private fun parsePrimary(): Expression {
        if (match(TokenType.NUMBER)) {
            val value = previous().value.toDouble()
            return NumberExpression(value)
        }

        if (match(TokenType.ID)) {
            return VariableExpression(previous().value)
        }

        if (match(TokenType.LPAREN)) {
            val expr = parseExpression()
            consume(TokenType.RPAREN, "Ожидается ')' после выражения.")
            return expr
        }

        throw Exception("[Parser Error] Line ${peek().line}, Col ${peek().column}: Ожидается выражение.")
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
        throw Exception("[Parser Error] Line ${token}, Col ${token.column}: $message")
    }
}