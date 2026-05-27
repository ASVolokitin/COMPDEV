package parser.ast.statement

class FunctionStatement(
    val name: String,
    val parameters: List<String>,
    val body: BlockStatement
) : Statement()
