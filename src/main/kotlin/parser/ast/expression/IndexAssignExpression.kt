package parser.ast.expression

class IndexAssignExpression(
    val array: Expression,
    val index: Expression,
    val value: Expression
) : Expression()
