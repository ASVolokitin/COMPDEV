package parser.ast.expression

import org.example.lexer.enums.TokenType

class UnaryExpression(
    val operator: TokenType,
    val right: Expression
) : Expression()
