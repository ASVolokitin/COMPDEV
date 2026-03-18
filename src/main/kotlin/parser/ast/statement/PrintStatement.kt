package parser.ast.statement

import parser.ast.expression.Expression

class PrintStatement(val expression: Expression) : Statement()
