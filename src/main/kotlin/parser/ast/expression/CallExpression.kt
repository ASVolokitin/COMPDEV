package parser.ast.expression

class CallExpression(
    val callee: String,
    val arguments: List<Expression>
) : Expression()
