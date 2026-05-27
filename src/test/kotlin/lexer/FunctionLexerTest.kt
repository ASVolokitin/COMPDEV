package lexer

import lexer.models.Lexer
import org.example.lexer.enums.TokenType
import org.junit.jupiter.api.Test
import support.TestProgramLoader
import kotlin.test.assertEquals

class FunctionLexerTest {

    @Test
    fun `test tokenize function declaration`() {
        val code = TestProgramLoader.readProgram("lexer/functions/tokenize_function_declaration.txt")
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()

        val actualTypes = tokens.map { it.tokenType }
        val expectedTypes = listOf(
            TokenType.FUN, TokenType.ID, TokenType.LPAREN, TokenType.ID, TokenType.COMMA, TokenType.ID, TokenType.RPAREN,
            TokenType.LBRACE, TokenType.RETURN, TokenType.ID, TokenType.PLUS, TokenType.ID, TokenType.SEMICOLON,
            TokenType.RBRACE, TokenType.EOF
        )

        assertEquals(expectedTypes, actualTypes)
    }
}
