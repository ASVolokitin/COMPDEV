package support

import java.io.File

/**
 * Utility class for loading test program files
 */
class TestProgramLoader {
    companion object {
        private val baseDir = File("src/test/resources")

        /**
         * Read a program file from test resources
         */
        fun readProgram(path: String): String {
            val file = File(baseDir, path)
            return file.readText()
        }
    }
}
