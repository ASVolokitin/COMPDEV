package interpreter

import exception.InterpreterException
import lexer.models.Lexer
import org.junit.jupiter.api.Test
import parser.Parser
import support.TestProgramLoader
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FunctioInterpreterTest {

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
    fun `test function call`() {
        val result = executeProgram("interpreter/functions/function_call.txt")
        assertEquals("15", result)
    }

    @Test
    fun `test recursive function`() {
        val result = executeProgram("interpreter/functions/recursive_function.txt")
        assertEquals("120", result)
    }

    @Test
    fun `test function without return value`() {
        val result = executeProgram("interpreter/functions/function_without_return_value.txt")
        assertEquals("null", result)
    }

    @Test
    fun `test function argument count mismatch`() {
        val error = assertFailsWith<InterpreterException> {
            executeProgram("interpreter/functions/function_argument_count_mismatch.txt")
        }

        assertEquals("Function 'add' expects 2 arguments, got 1", error.message)
    }
}
