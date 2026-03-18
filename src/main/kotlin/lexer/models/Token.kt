package org.example.lexer.models

import org.example.lexer.enums.TokenType

data class Token(
    val tokenType: TokenType,
    val value: String,
    val position: Int,
    val line: Int,
    val column: Int
) {

    override fun toString(): String {
        return "[$line:$column] Token($tokenType, '$value')"
    }
}
