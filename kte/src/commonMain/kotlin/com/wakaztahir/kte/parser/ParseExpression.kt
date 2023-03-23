package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.PrimitiveValue
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.increment

enum class ArithmeticOperatorType(val char: Char, val precedence: Int) {

    Plus('+', 6) {
        override fun operate(value1: Int, value2: Int): Int {
            return value1 + value2
        }

        override fun operate(value1: Float, value2: Float): Float {
            return value1 + value2
        }
    },
    Minus('-', 6) {
        override fun operate(value1: Int, value2: Int): Int {
            return value1 - value2
        }

        override fun operate(value1: Float, value2: Float): Float {
            return value1 - value2
        }
    },
    Divide('/', 4) {
        override fun operate(value1: Int, value2: Int): Int {
            return value1 / value2
        }

        override fun operate(value1: Float, value2: Float): Float {
            return value1 / value2
        }
    },
    Multiply('*', 4) {
        override fun operate(value1: Int, value2: Int): Int {
            return value1 * value2
        }

        override fun operate(value1: Float, value2: Float): Float {
            return value1 * value2
        }
    },
    Mod('%', 4) {
        override fun operate(value1: Int, value2: Int): Int {
            return value1 % value2
        }

        override fun operate(value1: Float, value2: Float): Float {
            return value1 % value2
        }
    };

    abstract fun operate(value1: Int, value2: Int): Int
    abstract fun operate(value1: Float, value2: Float): Float

}

internal fun SourceStream.parseArithmeticOperator(): ArithmeticOperatorType? {
    val result = when (currentChar) {
        '+' -> ArithmeticOperatorType.Plus
        '-' -> ArithmeticOperatorType.Minus
        '/' -> ArithmeticOperatorType.Divide
        '*' -> ArithmeticOperatorType.Multiply
        '%' -> ArithmeticOperatorType.Mod
        else -> null
    }
    if (result != null) incrementPointer()
    return result
}