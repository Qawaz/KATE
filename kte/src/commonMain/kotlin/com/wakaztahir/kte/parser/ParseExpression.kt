package com.wakaztahir.kte.parser

import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.increment

enum class ArithmeticOperatorType(val char: Char) {
    Plus('+'),
    Minus('-'),
    Divide('-'),
    Multiply('*'),
    Mod('%');

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