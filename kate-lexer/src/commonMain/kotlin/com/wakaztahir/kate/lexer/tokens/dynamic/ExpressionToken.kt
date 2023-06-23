package com.wakaztahir.kate.lexer.tokens.dynamic

import com.wakaztahir.kate.lexer.model.TokenConverter
import com.wakaztahir.kate.model.expression.ArithmeticOperatorType

class ExpressionToken(val first: ValueToken, val operator: ArithmeticOperatorType, val second: ValueToken) : ValueToken {
    override fun <T> convert(converter: TokenConverter<T>): T {
        return converter.convert(this)
    }
}