
import lexer.models.Lexer
import org.junit.jupiter.api.Test
import parser.Parser
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParserTest {

    @Test
    fun `test unused variable reports error`() {
        val code = "var x = 5;"
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        parser.parse()
        assertEquals(1, parser.errors.size)
        assertEquals("Переменная 'x' не используется", parser.errors[0])
    }

    @Test
    fun `test uninitialized variable reports error`() {
        val code = "var x; print x;"
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        parser.parse()
        assertEquals(1, parser.errors.size)
        assertEquals("Переменная 'x' не инициализирована", parser.errors[0])
    }

    @Test
    fun `test used and initialized variable has no errors`() {
        val code = "var x = 5; print x;"
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        parser.parse()
        assertTrue(parser.errors.isEmpty())
    }

    @Test
    fun `test complex expressions has no errors`() {
        val code = "var a = 10; var b = 20; print (a + b) * 2;"
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        parser.parse()
        assertTrue(parser.errors.isEmpty())
    }

    @Test
    fun `test nested blocks has no errors`() {
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
        parser.parse()
        assertTrue(parser.errors.isEmpty())
    }

    @Test
    fun `test missing semicolon reports error`() {
        val code = "var x = 5"
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        parser.parse()
        assertEquals(1, parser.errors.size)
        assertTrue(parser.errors[0].contains("Ожидается ';'"))
    }

    @Test
    fun `test invalid assignment target reports error`() {
        val code = "5 = x;"
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        parser.parse()
        assertEquals(1, parser.errors.size)
        assertTrue(parser.errors[0].contains("Недопустимая цель для присваивания"))
    }
}
