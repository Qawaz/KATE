package com.wakaztahir.kate.lexer.tokens.dynamic

import com.wakaztahir.kate.lexer.model.DynamicToken
import com.wakaztahir.kate.lexer.model.TokenConverter

data class ErrorToken(
    val exception: Throwable,
    val lineNumber: Int,
    val columnNumber: Int
) : DynamicToken {
    override fun <T> convert(converter: TokenConverter<T>): T {
        return converter.convert(this)
    }
}