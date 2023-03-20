package com.wakaztahir.kte.parser

import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.increment

enum class ArithmeticOperatorType(val char: Char) {

    Plus('+') {
        override fun operate(value1: Int, value2: Int): Int {
            return value1 + value2
        }
    },
    Minus('-') {
        override fun operate(value1: Int, value2: Int): Int {
            return value1 - value2
        }
    },
    Divide('/') {
        override fun operate(value1: Int, value2: Int): Int {
            return value1 / value2
        }
    },
    Multiply('*') {
        override fun operate(value1: Int, value2: Int): Int {
            return value1 * value2
        }
    },
    Mod('%') {
        override fun operate(value1: Int, value2: Int): Int {
            return value1 % value2
        }
    };

    abstract fun operate(value1: Int, value2: Int): Int

    fun parse(stream: SourceStream): ArithmeticOperatorType? {
        return if (stream.increment(char)) this@ArithmeticOperatorType else null
    }

}

internal fun SourceStream.parseArithmeticOperator(): ArithmeticOperatorType? {
    ArithmeticOperatorType.Plus.parse(this)?.let { return it }
    ArithmeticOperatorType.Minus.parse(this)?.let { return it }
    ArithmeticOperatorType.Divide.parse(this)?.let { return it }
    ArithmeticOperatorType.Multiply.parse(this)?.let { return it }
    ArithmeticOperatorType.Mod.parse(this)?.let { return it }
    return null
}