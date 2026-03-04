package org.example.models

import org.example.enums.TokenType

class Lexer {

    val input: String
    val length: Int
    var position: Int

    constructor(input: String) {
        this.input = input
        this.length = input.length
        this.position = 0
    }

    private fun peek(input: String): Char {
        if (position >= length) return 0.toChar()
        return input[position]
    }

    private fun next(): Char {
        if (position >= length) return 0.toChar()
        return input[position++]
    }

    private fun addToken(result: MutableList<Token>, type: TokenType, value: String, start: Int) {
        result.add(Token(type, value, start))
    }

    fun tokenize(): MutableList<Token> {

        val result = mutableListOf<Token>()

        while (position < length) {

            val current = peek(input)

            if (current.isWhitespace()) {
                next()
                continue
            }

            if (current.isDigit()) {
                tokenizeNumber(result)
                continue
            }

            if (current.isLetter()) {
                tokenizeWord(result)
                continue
            }

            tokenizeOperator(result)
        }


        return result
    }

    private fun tokenizeNumber(result: MutableList<Token>) {
        val start = position

        while (peek(input).isDigit()) next()

        val numberStr = input.substring(start, position)
        result.add(Token(TokenType.NUMBER, numberStr, start))
    }

    private fun tokenizeWord(result: MutableList<Token>) {

        val start = position
        while (peek(input).isLetterOrDigit()) next()
        when (val word = input.substring(start, position)) {
            "var" -> addToken(result, TokenType.VAR, word, start)
            "print" -> addToken(result, TokenType.PRINT, word, start)
            "if" -> addToken(result, TokenType.IF, word, start)
            "else" -> addToken(result, TokenType.ELSE, word, start)
            "while" -> addToken(result, TokenType.WHILE, word, start)
            else -> addToken(result, TokenType.ID, word, start)
        }
    }

    private fun tokenizeOperator(result: MutableList<Token>) {
        val current = peek(input)
        val start = position

        when (current) {
            '+' -> { next(); addToken(result, TokenType.PLUS, "+", start) }
            '-' -> { next(); addToken(result, TokenType.MINUS, "-", start) }
            '*' -> { next(); addToken(result, TokenType.STAR, "*", start) }
            '/' -> { next(); addToken(result, TokenType.SLASH, "/", start) }
            '=' -> { next(); addToken(result, TokenType.EQ, "=", start) }
            ';' -> { next(); addToken(result, TokenType.SEMICOLON, ";", start) }
            else -> throw IllegalArgumentException("Unexpected character '$current' at position $position")
        }
    }

}