import lexer.models.Lexer
import org.junit.jupiter.api.Test
import parser.Parser
import support.TestProgramLoader
import kotlin.test.assertTrue

class FunctionParserTest {

    @Test
    fun `test function declaration and call has no errors`() {
        val code = TestProgramLoader.readProgram("parser/functions/function_declaration_and_call.txt")
        val lexer = Lexer(code)
        val tokens = lexer.tokenize().toList()
        val parser = Parser(tokens)
        parser.parse()
        assertTrue(parser.errors.isEmpty())
    }
}
