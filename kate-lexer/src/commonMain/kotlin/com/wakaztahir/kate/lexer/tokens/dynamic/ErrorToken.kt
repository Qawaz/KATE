package com.wakaztahir.kate.lexer.tokens.dynamic

import com.wakaztahir.kate.lexer.model.DynamicToken
import com.wakaztahir.kate.lexer.model.TokenConverter
import com.wakaztahir.kate.lexer.stream.SequentialStream

data class ErrorToken(
    val exception: Throwable,
    val lineNumber: Int,
    val columnNumber: Int
) : DynamicToken {
    constructor(exception: Throwable, stream: SequentialStream) : this(
        exception = exception,
        lineNumber = stream.lineNumber,
        columnNumber = stream.columnNumber
    )
    override fun <T> convert(converter: TokenConverter<T>): T {
        return converter.convert(this)
    }
    fun throwError() : Nothing {
        throw exception
    }
}