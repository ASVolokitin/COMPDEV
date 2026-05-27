package lexer

import lexer.models.Lexer
import org.example.lexer.enums.TokenType
import org.junit.jupiter.api.Test
import support.TestProgramLoader
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LexerTest {

    @Test
    fun `test invalid character throws exception`() {
        val code = TestProgramLoader.readProgram("lexer/invalid_character.txt")
        val lexer = Lexer(code)
        try {
            lexer.tokenize().toList()
        } catch (e: Exception) {
            assertEquals("[Lexer Error] Unexpected character '@' at Line 1, Column 9", e.message)
        }
    }

    @Test
    fun `test tokenize typed declarations with string and boolean`() {
        val code = TestProgramLoader.readProgram("lexer/tokenize_typed_declarations.txt")
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()

        val actualTypes = tokens.map { it.tokenType }
        val expectedTypes = listOf(
            TokenType.VAR, TokenType.ID, TokenType.COLON, TokenType.ID, TokenType.EQ, TokenType.STRING, TokenType.SEMICOLON,
            TokenType.VAR, TokenType.ID, TokenType.COLON, TokenType.ID, TokenType.EQ, TokenType.TRUE, TokenType.SEMICOLON,
            TokenType.EOF
        )

        assertEquals(expectedTypes, actualTypes)
        assertEquals("Sasha", tokens[5].value)
    }

    @Test
    fun `test unterminated string throws exception`() {
        val code = TestProgramLoader.readProgram("lexer/unterminated_string.txt")
        val lexer = Lexer(code)

        val error = assertFailsWith<Exception> {
            lexer.tokenize().toList()
        }

        assertEquals("[Lexer Error] Unterminated string at Line 1, Column 12", error.message)
    }
}
