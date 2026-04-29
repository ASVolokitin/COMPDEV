package parser.ast.expression

class AssignExpression(
    val name: String,
    val value: Expression
) : Expression()
