
import lexer.models.Lexer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import parser.Parser
import java.text.ParseException

class ParserTest {

    @Test
    fun `test unused variable throws exception`() {
        val code = "var x = 5;"
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        assertThrows<ParseException> {
            parser.parse()
        }
    }

    @Test
    fun `test uninitialized variable throws exception`() {
        val code = "var x; print x;"
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        assertThrows<ParseException> {
            parser.parse()
        }
    }

    @Test
    fun `test used and initialized variable does not throw exception`() {
        val code = "var x = 5; print x;"
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        assertDoesNotThrow {
            parser.parse()
        }
    }

    @Test
    fun `test complex expressions`() {
        val code = "var a = 10; var b = 20; print (a + b) * 2;"
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        assertDoesNotThrow {
            parser.parse()
        }
    }

    @Test
    fun `test nested blocks`() {
        val code = """
            var x = 1;
            if (x == 1) {
                var y = 2;
                print y;
            }
            print x;
        """.trimIndent()
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        assertDoesNotThrow {
            parser.parse()
        }
    }

    @Test
    fun `test missing semicolon throws exception`() {
        val code = "var x = 5"
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        assertThrows<ParseException> {
            parser.parse()
        }
    }

    @Test
    fun `test invalid assignment target throws exception`() {
        val code = "5 = x;"
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        assertThrows<ParseException> {
            parser.parse()
        }
    }
}
