package parser.ast.statement

import parser.ast.expression.Expression

class VarStatement(val name: String, val initializer: Expression?) : Statement()
