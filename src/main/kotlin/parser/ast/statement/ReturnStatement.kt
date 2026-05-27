package parser.ast.statement

import parser.ast.expression.Expression

class ReturnStatement(val value: Expression?) : Statement()
