package parser.ast.statement

import parser.ast.expression.Expression

class WhileStatement(val condition: Expression, val body: Statement) : Statement()
