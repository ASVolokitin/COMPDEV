package parser.ast.expression

/**
 * An assignment expression (e.g., x = 10 + 5)
 * Why is this an Expression? To allow things like: a = b = 5;
 */
class AssignExpression(
    val name: String, // The name of the variable to write to
    val value: Expression // What to write
) : Expression()
