package parser.ast.statement

import parser.ast.expression.Expression

class IfStatement(
    val condition: Expression,
    val thenBranch: Statement,
    val elseBranch: Statement?
) : Statement()
