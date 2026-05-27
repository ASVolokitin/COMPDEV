package interpreter

import exception.ControlFlowException

class ReturnValue(val value: ValueType) : ControlFlowException()
