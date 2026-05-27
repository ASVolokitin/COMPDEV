package exception

class ArrayTypeMismatchException(line: Int) :
    ParserException("Array elements must have the same type.", line)
