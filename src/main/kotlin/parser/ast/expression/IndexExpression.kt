package parser.ast.expression

class IndexExpression(
    val array: Expression,
    val index: Expression
) : Expression()
