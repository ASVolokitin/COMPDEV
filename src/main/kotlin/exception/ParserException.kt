package exception

open class ParserException(message: String, val line: Int) : RuntimeException(message)
