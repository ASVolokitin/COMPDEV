package org.example.lexer.enums

enum class TokenType {
    NUMBER,

    ID,
    STRING,
    TRUE,
    FALSE,
    VAR,

    PRINT,

    IF,
    ELSE,
    WHILE,

    PLUS, MINUS, STAR, SLASH,
    EQ, EQEQ, EXCL, NEQ,
    LT, GT, LTEQ, GREQ,
    AND, OR,

    LPAREN, RPAREN,
    LBRACE, RBRACE,
    SEMICOLON,
    COLON,

    EOF

}