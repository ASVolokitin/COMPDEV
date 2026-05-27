package exception

class ArrayIndexOutOfBoundsException(index: Int, size: Int) :
    ArrayRuntimeException("Array index $index out of bounds for length $size")
