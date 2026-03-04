package org.example.models

import org.example.enums.TokenType

class Token(val tokenType: TokenType, val value: String, val position: Int) {

    override fun toString(): String {
        return "Token(Type: $tokenType, Value: '$value') at {$position}";
    }
}