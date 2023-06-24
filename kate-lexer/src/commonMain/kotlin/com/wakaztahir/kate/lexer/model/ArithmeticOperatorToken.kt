package com.wakaztahir.kate.lexer.model

import com.wakaztahir.kate.model.expression.ArithmeticOperatorType

class ArithmeticOperatorToken(val operator : ArithmeticOperatorType) : DynamicToken {
    override fun <T> convert(converter: TokenConverter<T>): T {
        return converter.convert(this)
    }
}