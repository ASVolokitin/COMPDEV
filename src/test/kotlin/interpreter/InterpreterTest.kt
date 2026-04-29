package interpreter

import lexer.models.Lexer
import parser.Parser
import org.junit.jupiter.api.Test
import support.TestProgramLoader
import kotlin.test.assertEquals

class InterpreterTest {

    private fun executeProgram(path: String): String {
        val code = TestProgramLoader.readProgram(path)
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        val statements = parser.parse()
        val interpreter = Interpreter()
        interpreter.interpret(statements)
        return interpreter.getOutput().trim()
    }

    @Test
    fun `test number literal`() {
        val result = executeProgram("interpreter/number_literal.txt")
        assertEquals("42", result)
    }

    @Test
    fun `test decimal number literal`() {
        val result = executeProgram("interpreter/decimal_number.txt")
        assertEquals("3.14", result)
    }

    @Test
    fun `test addition`() {
        val result = executeProgram("interpreter/addition.txt")
        assertEquals("5", result)
    }

    @Test
    fun `test subtraction`() {
        val result = executeProgram("interpreter/subtraction.txt")
        assertEquals("6", result)
    }

    @Test
    fun `test multiplication`() {
        val result = executeProgram("interpreter/multiplication.txt")
        assertEquals("42", result)
    }

    @Test
    fun `test division`() {
        val result = executeProgram("interpreter/division.txt")
        assertEquals("5", result)
    }

    @Test
    fun `test operator precedence`() {
        val result = executeProgram("interpreter/operator_precedence.txt")
        assertEquals("14", result)
    }

    @Test
    fun `test parentheses precedence`() {
        val result = executeProgram("interpreter/parentheses_precedence.txt")
        assertEquals("20", result)
    }

    @Test
    fun `test unary minus`() {
        val result = executeProgram("interpreter/unary_minus.txt")
        assertEquals("-5", result)
    }

    @Test
    fun `test unary minus with expression`() {
        val result = executeProgram("interpreter/unary_minus_expression.txt")
        assertEquals("-5", result)
    }

    @Test
    fun `test string literal`() {
        val result = executeProgram("interpreter/string_literal.txt")
        assertEquals("hello", result)
    }

    @Test
    fun `test string concatenation with plus`() {
        val result = executeProgram("interpreter/string_concatenation.txt")
        assertEquals("hello world", result)
    }

    @Test
    fun `test number and string concatenation`() {
        val result = executeProgram("interpreter/number_string_concatenation.txt")
        assertEquals("value: 42", result)
    }

    @Test
    fun `test boolean true`() {
        val result = executeProgram("interpreter/boolean_true.txt")
        assertEquals("true", result)
    }

    @Test
    fun `test boolean false`() {
        val result = executeProgram("interpreter/boolean_false.txt")
        assertEquals("false", result)
    }
}
