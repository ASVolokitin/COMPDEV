package lexer.models

import exception.LexerException
import org.example.lexer.enums.TokenType
import org.example.lexer.models.Token

class Lexer(private val input: String) {
    private var position: Int = 0
    private var line: Int = 1
    private var column: Int = 1

    private val keywords = mapOf(
        "var" to TokenType.VAR,
        "print" to TokenType.PRINT,
        "if" to TokenType.IF,
        "else" to TokenType.ELSE,
        "while" to TokenType.WHILE,
        "fun" to TokenType.FUN,
        "return" to TokenType.RETURN,
        "true" to TokenType.TRUE,
        "false" to TokenType.FALSE
    )

    private val operators = mapOf(
        "==" to TokenType.EQEQ,
        "!=" to TokenType.NEQ,
        "<=" to TokenType.LTEQ,
        ">=" to TokenType.GREQ,
        "&&" to TokenType.AND,
        "||" to TokenType.OR,
        "+" to TokenType.PLUS,
        "-" to TokenType.MINUS,
        "*" to TokenType.STAR,
        "/" to TokenType.SLASH,
        "=" to TokenType.EQ,
        "<" to TokenType.LT,
        ">" to TokenType.GT,
        "!" to TokenType.EXCL,
        "(" to TokenType.LPAREN,
        ")" to TokenType.RPAREN,
        "{" to TokenType.LBRACE,
        "}" to TokenType.RBRACE,
        "[" to TokenType.LBRACKET,
        "]" to TokenType.RBRACKET,
        ";" to TokenType.SEMICOLON,
        "," to TokenType.COMMA,
        ":" to TokenType.COLON
    )

    fun tokenize(): Sequence<Token> = sequence {
        while (position < input.length) {
            val current = peek()

            if (current.isWhitespace()) {
                next()
                continue
            }

            if (current.isDigit()) {
                yield(readNumber())
                continue
            }

            if (current == '"') {
                yield(readString())
                continue
            }

            if (current.isLetter()) {
                yield(readWord())
                continue
            }

            yield(readOperatorOrPunctuation())
        }

        yield(Token(TokenType.EOF, "\u0000", position, line, column))
    }

    private fun readNumber(): Token {
        val startPos = position
        val startLine = line
        val startCol = column

        while (peek().isDigit()) {
            next()
        }

        if (peek() == '.' && (position + 1 < input.length && input[position + 1].isDigit())) {
            next()
            while (peek().isDigit()) {
                next()
            }
        }

        val text = input.substring(startPos, position)
        return Token(TokenType.NUMBER, text, startPos, startLine, startCol)
    }

    private fun readWord(): Token {
        val startPos = position
        val startLine = line
        val startCol = column

        while (peek().isLetterOrDigit()) {
            next()
        }

        val text = input.substring(startPos, position)
        val type = keywords.getOrDefault(text, TokenType.ID)
        return Token(type, text, startPos, startLine, startCol)
    }

    private fun readString(): Token {
        val startPos = position
        val startLine = line
        val startCol = column

        next()
        val valueStart = position

        while (peek() != '"' && peek() != '\u0000' && peek() != '\n') {
            next()
        }

        if (peek() != '"') {
            throw LexerException("[Lexer Error] Unterminated string at Line $startLine, Column $startCol")
        }

        val text = input.substring(valueStart, position)
        next()

        return Token(TokenType.STRING, text, startPos, startLine, startCol)
    }

    private fun readOperatorOrPunctuation(): Token {
        val startPos = position
        val startLine = line
        val startCol = column

        if (position + 1 < input.length) {
            val twoChars = input.substring(position, position + 2)
            operators[twoChars]?.let {
                next()
                next()
                return Token(it, twoChars, startPos, startLine, startCol)
            }
        }

        val oneChar = input[position].toString()
        operators[oneChar]?.let {
            next()
            return Token(it, oneChar, startPos, startLine, startCol)
        }

        val badChar = peek()
        throw LexerException("[Lexer Error] Unexpected character '$badChar' at Line $startLine, Column $startCol")
    }

    private fun peek(): Char = if (position >= input.length) '\u0000' else input[position]

    private fun next(): Char {
        if (position >= input.length) return '\u0000'

        val current = input[position++]

        if (current == '\n') {
            line++
            column = 1
        } else {
            column++
        }

        return current
    }
}
