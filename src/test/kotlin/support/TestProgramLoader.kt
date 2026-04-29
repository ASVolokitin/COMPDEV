package support

object TestProgramLoader {
    fun readProgram(path: String): String {
        return javaClass.classLoader
            .getResource(path)
            ?.readText()
            ?: error("Test program '$path' not found")
    }
}
