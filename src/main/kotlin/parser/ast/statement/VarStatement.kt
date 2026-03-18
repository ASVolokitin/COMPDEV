package parser.ast.statement

import parser.ast.expression.Expression

class VarStatement(
    val name: String,
    val initializer: Expression?,
    var isUsed: Boolean = false,
    var isInitialized: Boolean = initializer != null
) : Statement()
