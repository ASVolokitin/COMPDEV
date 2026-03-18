package parser.ast.expression

import org.example.lexer.enums.TokenType

class BinaryExpression(
    val left: Expression,
    val operator: TokenType,
    val right: Expression
) : Expression()
