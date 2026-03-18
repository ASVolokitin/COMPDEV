package lexer

import lexer.models.Lexer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LexerTest {

    @Test
    fun `test invalid character throws exception`() {
        val code = "var a = @;"
        val lexer = Lexer(code)
        try {
            lexer.tokenize().toList()
        } catch (e: Exception) {
            assertEquals("[Lexer Error] Unexpected character '@' at Line 1, Column 9", e.message)
        }
    }
}
