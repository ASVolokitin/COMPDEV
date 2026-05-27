
import lexer.models.Lexer
import org.junit.jupiter.api.Test
import parser.Parser
import support.TestProgramLoader
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParserTest {

    @Test
    fun `test unused variable reports error`() {
        val code = TestProgramLoader.readProgram("parser/unused_variable.txt")
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        parser.parse()
        assertEquals(1, parser.errors.size)
        assertEquals("Variable 'x' is not used", parser.errors[0])
    }

    @Test
    fun `test uninitialized variable reports error`() {
        val code = TestProgramLoader.readProgram("parser/uninitialized_variable.txt")
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        parser.parse()
        assertEquals(1, parser.errors.size)
        assertEquals("Variable 'x' is not initialized", parser.errors[0])
    }

    @Test
    fun `test used and initialized variable has no errors`() {
        val code = TestProgramLoader.readProgram("parser/used_and_initialized_variable.txt")
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        parser.parse()
        assertTrue(parser.errors.isEmpty())
    }

    @Test
    fun `test complex expressions has no errors`() {
        val code = TestProgramLoader.readProgram("parser/complex_expressions.txt")
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        parser.parse()
        assertTrue(parser.errors.isEmpty())
    }

    @Test
    fun `test nested blocks has no errors`() {
        val code = TestProgramLoader.readProgram("parser/nested_blocks.txt")
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        parser.parse()
        assertTrue(parser.errors.isEmpty())
    }

    @Test
    fun `test missing semicolon reports error`() {
        val code = TestProgramLoader.readProgram("parser/missing_semicolon.txt")
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        parser.parse()
        assertEquals(1, parser.errors.size)
        assertTrue(parser.errors[0].contains("Expected ';'"))
    }

    @Test
    fun `test invalid assignment target reports error`() {
        val code = TestProgramLoader.readProgram("parser/invalid_assignment_target.txt")
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        parser.parse()
        assertEquals(1, parser.errors.size)
        assertTrue(parser.errors[0].contains("Invalid assignment target"))
    }

    @Test
    fun `test string and boolean literals has no errors`() {
        val code = TestProgramLoader.readProgram("parser/string_and_boolean_literals.txt")
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        parser.parse()
        assertTrue(parser.errors.isEmpty())
    }

    @Test
    fun `test typed variable declaration has no errors`() {
        val code = TestProgramLoader.readProgram("parser/typed_variable_declaration.txt")
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        parser.parse()
        assertTrue(parser.errors.isEmpty())
    }

    @Test
    fun `test unknown variable type reports error`() {
        val code = TestProgramLoader.readProgram("parser/unknown_variable_type.txt")
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        parser.parse()
        assertEquals(1, parser.errors.size)
        assertTrue(parser.errors[0].contains("Unknown type 'decimal'"))
    }

    @Test
    fun `test missing variable type after colon reports error`() {
        val code = TestProgramLoader.readProgram("parser/missing_type_after_colon.txt")
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        parser.parse()
        assertEquals(1, parser.errors.size)
        assertTrue(parser.errors[0].contains("Expected variable type after ':'"))
    }
}
