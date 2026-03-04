package org.example

import org.example.models.Lexer

fun main() {
    val codeExample = "var x = 123; print x + 5;"
    val lexer = Lexer(codeExample)
    val tokens = lexer.tokenize()

    for (token in tokens) {
        println(token)
    }
}