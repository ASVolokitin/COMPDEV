package optimizer

import lexer.models.Lexer
import org.junit.jupiter.api.Test
import parser.Parser
import support.TestProgramLoader
import kotlin.test.assertEquals

class AstOptimizerTest {
    @Test
    fun `prints ast before and after optimizing constants`() {
        val code = TestProgramLoader.readProgram("optimizer/constant_folding.txt")
        val parser = Parser(Lexer(code).tokenize().toList())
        val statements = parser.parse()
        val optimizedStatements = AstOptimizer().optimize(statements)
        val printer = AstPrinter()

        val before = printer.print(statements)
        val after = printer.print(optimizedStatements)

        println("Before optimization:")
        println(before)
        println("After optimization:")
        println(after)

        assertEquals(
            """
                var a = ((5 + 5) + 5);
                print a;
                print (("hello" + " ") + "world");
            """.trimIndent(),
            before
        )
        assertEquals(
            """
                var a = 15;
                print 15;
                print "hello world";
            """.trimIndent(),
            after
        )
    }

    @Test
    fun `propagates constants through program`() {
        val code = TestProgramLoader.readProgram("optimizer/constant_propagation.txt")
        val parser = Parser(Lexer(code).tokenize().toList())
        val statements = parser.parse()
        val optimizedStatements = AstOptimizer().optimize(statements)
        val printer = AstPrinter()

        val before = printer.print(statements)
        val after = printer.print(optimizedStatements)

        println("Before optimization:")
        println(before)
        println("After optimization:")
        println(after)

        assertEquals(
            """
                var a = 5;
                var b = (a + 10);
                print ("value: " + b);
                (b = (b + 1));
                print b;
            """.trimIndent(),
            before
        )
        assertEquals(
            """
                var a = 5;
                var b = 15;
                print "value: 15";
                (b = 16);
                print 16;
            """.trimIndent(),
            after
        )
    }
}
